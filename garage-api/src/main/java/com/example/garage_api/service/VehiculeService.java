package com.example.garage_api.service;

import com.example.garage_api.entity.Client;
import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.exception.ConflitException;
import com.example.garage_api.exception.IntrouvableException;
import com.example.garage_api.repository.InterventionRepository;
import com.example.garage_api.repository.VehiculeRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculeService {

    // Le "tableau des transitions" : pour chaque statut, la liste des statuts
    // vers lesquels on a le droit d'aller. Tout ce qui n'est pas ici est interdit.
    //   static final : un seul tableau pour tout le programme, qu'on ne modifie jamais.
    //   Map.of(cle, valeur, ...) : un dictionnaire "statut -> statuts autorises".
    //   Set.of(...) : un ensemble de valeurs ; Set.of() vide = "aucune sortie possible".
    private static final Map<StatutVehicule, Set<StatutVehicule>> TRANSITIONS = Map.of(
            StatutVehicule.EN_ATTENTE, Set.of(StatutVehicule.EN_REPARATION),
            StatutVehicule.EN_REPARATION, Set.of(StatutVehicule.TERMINE),
            StatutVehicule.TERMINE, Set.of(StatutVehicule.LIVRE, StatutVehicule.EN_REPARATION),
            StatutVehicule.LIVRE, Set.of()
    );

    // Les magasiniers et le service voisin dont on a besoin.
    private final VehiculeRepository vehiculeRepository;
    private final InterventionRepository interventionRepository;
    // On reutilise ClientService : la regle "client introuvable = 404" reste ecrite a UN seul endroit.
    private final ClientService clientService;

    // Constructeur : Spring fournit lui-meme les trois objets (injection de dependances).
    public VehiculeService(VehiculeRepository vehiculeRepository,
                           InterventionRepository interventionRepository,
                           ClientService clientService) {
        this.vehiculeRepository = vehiculeRepository;
        this.interventionRepository = interventionRepository;
        this.clientService = clientService;
    }

    // LISTE paginee : recherche + filtre de statut facultatif + filtre de client facultatif.
    @Transactional(readOnly = true)
    public Page<Vehicule> lister(String recherche, StatutVehicule statut,
                                 Long clientId, Pageable pageable) {
        // Sans texte, on cherche "" : cela correspond a tous les vehicules.
        String texte = (recherche == null) ? "" : recherche.trim();
        // statut et clientId peuvent etre null : la requete saute alors ces filtres.
        return vehiculeRepository.rechercher(texte, statut, clientId, pageable);
    }

    // LIRE un vehicule par son id.
    @Transactional(readOnly = true)
    public Vehicule lire(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new IntrouvableException("Vehicule introuvable : " + id));
    }

    // LISTE des vehicules d'UN client, sans pagination (route /api/clients/{id}/vehicules).
    @Transactional(readOnly = true)
    public List<Vehicule> listerParClient(Long clientId) {
        // Regle : le client doit exister (404 sinon). lire() lance l'exception si besoin.
        clientService.lire(clientId);
        // On reutilise la recherche existante : texte vide, aucun filtre de statut.
        // Pageable.unpaged() = "toutes les lignes, sans decoupage en pages".
        return vehiculeRepository.rechercher("", null, clientId, Pageable.unpaged()).getContent();
    }


    // CREER un vehicule. L'API recevra un clientId (pas le client entier) :
    // c'est pourquoi le clientId est un parametre a part.
    @Transactional
    public Vehicule creer(Vehicule vehicule, Long clientId) {
        // Regle : le client doit exister (404 sinon). lire() lance l'exception si besoin.
        Client proprietaire = clientService.lire(clientId);

        // Regle : l'immatriculation doit etre unique.
        if (vehiculeRepository.existsByImmatriculation(vehicule.getImmatriculation())) {
            throw new ConflitException("Cette immatriculation existe deja : " + vehicule.getImmatriculation());
        }

        vehicule.setClient(proprietaire);
        // Un vehicule qui arrive commence toujours en attente.
        vehicule.setStatut(StatutVehicule.EN_ATTENTE);
        return vehiculeRepository.save(vehicule);
    }

    // MODIFIER un vehicule existant (sans toucher a son statut).
    @Transactional
    public Vehicule modifier(Long id, Vehicule donnees, Long clientId) {
        // 404 si le vehicule n'existe pas, puis 404 si le client n'existe pas.
        Vehicule existant = lire(id);
        Client proprietaire = clientService.lire(clientId);

        // Regle : l'immatriculation ne doit pas appartenir a UN AUTRE vehicule.
        if (vehiculeRepository.existsByImmatriculationAndIdNot(donnees.getImmatriculation(), id)) {
            throw new ConflitException("Cette immatriculation existe deja : " + donnees.getImmatriculation());
        }

        // On recopie champ par champ. On ne recopie PAS le statut : seul changerStatut le modifie.
        existant.setClient(proprietaire);
        existant.setImmatriculation(donnees.getImmatriculation());
        existant.setMarque(donnees.getMarque());
        existant.setModele(donnees.getModele());
        existant.setAnnee(donnees.getAnnee());
        existant.setKilometrage(donnees.getKilometrage());
        existant.setCarburant(donnees.getCarburant());
        existant.setDateEntree(donnees.getDateEntree());
        existant.setCoutEstime(donnees.getCoutEstime());
        existant.setPanne(donnees.getPanne());
        return vehiculeRepository.save(existant);
    }

    // CHANGER LE STATUT d'un vehicule, seulement selon les transitions permises.
    @Transactional
    public Vehicule changerStatut(Long id, StatutVehicule nouveauStatut) {
        Vehicule vehicule = lire(id);

        // On lit dans le tableau : "depuis le statut actuel, ou a-t-on le droit d'aller ?"
        Set<StatutVehicule> autorises = TRANSITIONS.get(vehicule.getStatut());

        // Regle : si le nouveau statut n'est pas dans la liste, on refuse (409).
        // Cela refuse aussi de "changer" vers le meme statut.
        if (!autorises.contains(nouveauStatut)) {
            throw new ConflitException("Transition interdite : "
                    + vehicule.getStatut() + " vers " + nouveauStatut);
        }

        vehicule.setStatut(nouveauStatut);
        return vehiculeRepository.save(vehicule);
    }

    // SUPPRIMER un vehicule.
    @Transactional
    public void supprimer(Long id) {
        Vehicule vehicule = lire(id);

        // Regle : suppression refusee (409) si le vehicule a des interventions.
        if (interventionRepository.existsByVehiculeId(id)) {
            throw new ConflitException("Suppression impossible : ce vehicule a des interventions.");
        }
        vehiculeRepository.delete(vehicule);
    }



}