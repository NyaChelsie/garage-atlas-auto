package com.example.garage_api.service;

import com.example.garage_api.entity.Intervention;
import com.example.garage_api.entity.LigneIntervention;
import com.example.garage_api.entity.Piece;
import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.exception.ConflitException;
import com.example.garage_api.exception.IntrouvableException;
import com.example.garage_api.repository.InterventionRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterventionService {

    // Le magasinier des interventions, et les deux services dont on a besoin.
    private final InterventionRepository interventionRepository;
    // On reutilise VehiculeService et PieceService : chaque regle reste ecrite a UN seul endroit.
    private final VehiculeService vehiculeService;
    private final PieceService pieceService;

    // Constructeur : Spring fournit lui-meme les trois objets (injection de dependances).
    public InterventionService(InterventionRepository interventionRepository,
                               VehiculeService vehiculeService,
                               PieceService pieceService) {
        this.interventionRepository = interventionRepository;
        this.vehiculeService = vehiculeService;
        this.pieceService = pieceService;
    }

    // LIRE une intervention par son id.
    @Transactional(readOnly = true)
    public Intervention lire(Long id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new IntrouvableException("Intervention introuvable : " + id));
    }

    // HISTORIQUE d'un vehicule : ses interventions, de la plus recente a la plus ancienne.
    @Transactional(readOnly = true)
    public List<Intervention> listerParVehicule(Long vehiculeId) {
        // 404 si le vehicule n'existe pas (plutot que de renvoyer une liste vide trompeuse).
        vehiculeService.lire(vehiculeId);
        return interventionRepository.findByVehiculeIdOrderByDateDebutDesc(vehiculeId);
    }

    // CREER une intervention pour un vehicule (les lignes viendront ensuite, une par une).
    @Transactional
    public Intervention creer(Intervention intervention, Long vehiculeId) {
        // Regle : la date de fin ne peut pas etre avant la date de debut.
        verifierDates(intervention);

        // 404 si le vehicule n'existe pas.
        Vehicule vehicule = vehiculeService.lire(vehiculeId);

        // Regle : une intervention est refusee (409) si le vehicule est deja livre.
        if (vehicule.getStatut() == StatutVehicule.LIVRE) {
            throw new ConflitException("Intervention impossible : ce vehicule est deja livre.");
        }

        intervention.setVehicule(vehicule);
        return interventionRepository.save(intervention);
    }

    // MODIFIER une intervention existante (ses informations, pas ses lignes).
    @Transactional
    public Intervention modifier(Long id, Intervention donnees) {
        // Regle : la date de fin ne peut pas etre avant la date de debut.
        verifierDates(donnees);

        // 404 si l'intervention n'existe pas.
        Intervention existante = lire(id);

        // On recopie champ par champ. On ne change ni le vehicule (une intervention reste
        // attachee a son vehicule), ni les lignes (elles ont leurs propres operations).
        existante.setType(donnees.getType());
        existante.setDescription(donnees.getDescription());
        existante.setDateDebut(donnees.getDateDebut());
        existante.setDateFin(donnees.getDateFin());
        existante.setMainOeuvre(donnees.getMainOeuvre());
        return interventionRepository.save(existante);
    }

    // AJOUTER UNE LIGNE : une piece, en une certaine quantite, sur une intervention.
    // @Transactional : si UNE etape echoue, TOUT est annule (le stock ne baisse pas).
    // Une RuntimeException (nos deux exceptions metier en sont) declenche cette annulation.
    @Transactional
    public Intervention ajouterLigne(Long interventionId, Long pieceId, int quantite) {
        // 404 si l'intervention n'existe pas.
        Intervention intervention = lire(interventionId);

        // Regle : une meme piece ne figure qu'une fois par intervention
        // (la base l'impose aussi : couple intervention + piece unique).
        // anyMatch : "y a-t-il au moins une ligne dont la piece a cet id ?"
        // "l -> ..." est une lambda : "pour chaque ligne l, pose cette question".
        boolean dejaPresente = intervention.getLignes().stream()
                .anyMatch(l -> l.getPiece().getId().equals(pieceId));
        if (dejaPresente) {
            throw new ConflitException("Cette piece figure deja sur l'intervention.");
        }

        // ETAPE 1 : verifier le stock ET le diminuer.
        // sortirStock lance un 404 (piece inconnue) ou un 409 (stock insuffisant) si besoin,
        // et renvoie la piece mise a jour.
        Piece piece = pieceService.sortirStock(pieceId, quantite);

        // ETAPE 2 : creer la ligne, avec le prix ACTUEL de la piece, copie pour toujours.
        LigneIntervention ligne = new LigneIntervention();
        ligne.setPiece(piece);
        ligne.setQuantite(quantite);
        ligne.setPrixUnitaire(piece.getPrixUnitaire());

        // ajouterLigne relie les deux cotes : la liste de l'intervention ET le champ de la ligne.
        intervention.ajouterLigne(ligne);

        // save sur l'intervention enregistre aussi la ligne (cascade).
        return interventionRepository.save(intervention);
    }

    // RETIRER UNE LIGNE : le stock est restitue.
    @Transactional
    public Intervention retirerLigne(Long interventionId, Long ligneId) {
        // 404 si l'intervention n'existe pas.
        Intervention intervention = lire(interventionId);

        // On cherche la ligne DANS cette intervention (on ne peut pas retirer la ligne d'une autre).
        // findFirst : la premiere ligne qui correspond, dans une "boite" (Optional).
        LigneIntervention ligne = intervention.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new IntrouvableException("Ligne introuvable : " + ligneId));

        // Le stock est restitue : on remet la quantite de la ligne dans la piece.
        pieceService.entrerStock(ligne.getPiece().getId(), ligne.getQuantite());

        // On retire la ligne de la liste : la suppression des orphelines l'efface de la base.
        intervention.getLignes().remove(ligne);
        return interventionRepository.save(intervention);
    }

    // SUPPRIMER une intervention : tout le stock de ses lignes est restitue.
    @Transactional
    public void supprimer(Long id) {
        // 404 si l'intervention n'existe pas.
        Intervention intervention = lire(id);

        // On remet en stock la quantite de CHAQUE ligne, avant de supprimer.
        for (LigneIntervention ligne : intervention.getLignes()) {
            pieceService.entrerStock(ligne.getPiece().getId(), ligne.getQuantite());
        }

        // delete supprime aussi les lignes (cascade = ALL).
        interventionRepository.delete(intervention);
    }

    // Regle du sujet : la date de fin, si elle existe, ne peut pas etre avant la date de debut.
    // Une annotation ne sait pas comparer deux champs : la regle vit donc ici, dans le service.
    // IllegalArgumentException = "donnee invalide" ; l'etape 5 la traduira en reponse 400.
    private void verifierDates(Intervention intervention) {
        // On teste d'abord que les deux dates existent : la date de fin est facultative.
        if (intervention.getDateDebut() != null
                && intervention.getDateFin() != null
                && intervention.getDateFin().isBefore(intervention.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas etre avant la date de debut.");
        }
    }
}