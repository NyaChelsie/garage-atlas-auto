package com.example.garage_api.service;

import com.example.garage_api.entity.Client;
import com.example.garage_api.exception.ConflitException;
import com.example.garage_api.exception.IntrouvableException;
import com.example.garage_api.repository.ClientRepository;
import com.example.garage_api.repository.VehiculeRepository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// Service : "Spring, cree un objet de cette classe et gere-le toi-meme".
// Comme @Component, mais le nom dit le role : ici, la couche des regles metier.
import org.springframework.stereotype.Service;
// Transactional : tout ce qui se passe dans la methode reussit ensemble,
// ou rien n'est enregistre (retour en arriere automatique en cas d'exception).
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    // Les deux magasiniers dont ce service a besoin. final = on ne les remplace jamais.
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;

    // Constructeur : Spring fournit lui-meme les deux repositories (injection de dependances).
    public ClientService(ClientRepository clientRepository,
                         VehiculeRepository vehiculeRepository) {
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    // LISTE paginee avec recherche (nom, prenom ou telephone).
    // readOnly = true : on ne fait que lire, la base peut etre plus rapide.
    @Transactional(readOnly = true)
    public Page<Client> lister(String recherche, Pageable pageable) {
        // Si aucun texte n'est donne, on cherche "" : cela correspond a tous les clients.
        // trim() retire les espaces au debut et a la fin.
        String texte = (recherche == null) ? "" : recherche.trim();
        return clientRepository.rechercher(texte, pageable);
    }

    // LIRE un client par son id.
    @Transactional(readOnly = true)
    public Client lire(Long id) {
        // findById renvoie un Optional : une "boite" qui contient le client, ou qui est vide.
        // orElseThrow : si la boite est vide, on lance l'exception (404 a l'etape 5).
        // "() -> new ..." est une lambda : "fabrique l'exception seulement si on en a besoin".
        return clientRepository.findById(id)
                .orElseThrow(() -> new IntrouvableException("Client introuvable : " + id));
    }

    // CREER un client.
    @Transactional
    public Client creer(Client client) {
        // Regle : le telephone doit etre unique.
        if (clientRepository.existsByTelephone(client.getTelephone())) {
            throw new ConflitException("Ce telephone est deja utilise : " + client.getTelephone());
        }
        // Si l'appelant ne donne pas de date d'inscription, on prend celle d'aujourd'hui.
        // Sinon on garde la sienne (le controle "pas dans le futur" viendra avec la validation, etape 5).
        if (client.getDateInscription() == null) {
            client.setDateInscription(LocalDate.now());
        }
        // save renvoie le client AVEC son id genere.
        return clientRepository.save(client);
    }

    // MODIFIER un client existant.
    @Transactional
    public Client modifier(Long id, Client donnees) {
        // 404 si le client n'existe pas.
        Client existant = lire(id);

        // Regle : le telephone ne doit pas appartenir a UN AUTRE client.
        // Le client peut garder son propre numero (c'est le role de IdNot).
        if (clientRepository.existsByTelephoneAndIdNot(donnees.getTelephone(), id)) {
            throw new ConflitException("Ce telephone est deja utilise : " + donnees.getTelephone());
        }

        // On recopie champ par champ sur le client existant.
        // On ne touche pas a l'id. La date d'inscription n'est recopiee que si elle est fournie.
        existant.setNom(donnees.getNom());
        existant.setPrenom(donnees.getPrenom());
        existant.setTelephone(donnees.getTelephone());
        existant.setEmail(donnees.getEmail());
        existant.setVille(donnees.getVille());
        if (donnees.getDateInscription() != null) {
            existant.setDateInscription(donnees.getDateInscription());
        }
        return clientRepository.save(existant);
    }

    // SUPPRIMER un client.
    @Transactional
    public void supprimer(Long id) {
        // 404 si le client n'existe pas.
        Client client = lire(id);

        // Regle : suppression refusee (409) si le client possede des vehicules.
        if (vehiculeRepository.existsByClientId(id)) {
            throw new ConflitException("Suppression impossible : ce client possede des vehicules.");
        }
        clientRepository.delete(client);
    }
}