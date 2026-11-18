package com.example.garage_api.service;

import com.example.garage_api.dto.InterventionStatistiques;
import com.example.garage_api.repository.ClientRepository;
import com.example.garage_api.repository.InterventionRepository;
import com.example.garage_api.repository.LigneInterventionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatistiquesInterventionService {

    private final ClientRepository clientRepository;
    private final InterventionRepository interventionRepository;
    private final LigneInterventionRepository ligneRepository;

    public StatistiquesInterventionService(ClientRepository clientRepository,
                                           InterventionRepository interventionRepository,
                                           LigneInterventionRepository ligneRepository) {
        this.clientRepository = clientRepository;
        this.interventionRepository = interventionRepository;
        this.ligneRepository = ligneRepository;
    }

    // STATISTIQUES DES CLIENTS ET INTERVENTIONS.
    @Transactional(readOnly = true)
    public InterventionStatistiques statistiques() {
        long clients = clientRepository.count();
        long interventions = interventionRepository.count();

        // Chiffre d'affaires du mois = main-d'oeuvre + pieces des interventions commencees ce mois-ci.
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        BigDecimal chiffreAffaires = ouZero(interventionRepository.sommeMainOeuvreEntre(debut, fin))
                .add(ouZero(ligneRepository.sommeLignesEntre(debut, fin)));

        // Cout moyen = (toute la main-d'oeuvre + toutes les pieces) / nombre d'interventions.
        // Aucune intervention : on ne divise pas par 0, on renvoie 0.
        BigDecimal coutMoyen = BigDecimal.ZERO;
        if (interventions > 0) {
            BigDecimal totalGeneral = ouZero(interventionRepository.sommeMainOeuvre())
                    .add(ouZero(ligneRepository.sommeToutesLignes()));
            // divide(diviseur, 2 decimales, arrondi classique)
            coutMoyen = totalGeneral.divide(BigDecimal.valueOf(interventions), 2, RoundingMode.HALF_UP);
        }

        // TOP 5 DES CLIENTS : on additionne, par client, la main-d'oeuvre et les pieces.
        // Deux requetes GROUP BY renvoient UNE ligne par client (pas toute la table).
        Map<Long, String> noms = new HashMap<>();
        Map<Long, BigDecimal> montants = new HashMap<>();
        // Chaque ligne est un tableau [id, prenom, nom, total].
        for (Object[] ligne : interventionRepository.depensesMainOeuvreParClient()) {
            Long id = (Long) ligne[0];
            noms.put(id, ligne[1] + " " + ligne[2]);
            // merge : si l'id existe deja, additionne ; sinon, ajoute.
            montants.merge(id, ouZero((BigDecimal) ligne[3]), BigDecimal::add);
        }
        for (Object[] ligne : ligneRepository.depensesPiecesParClient()) {
            Long id = (Long) ligne[0];
            noms.put(id, ligne[1] + " " + ligne[2]);
            montants.merge(id, ouZero((BigDecimal) ligne[3]), BigDecimal::add);
        }
        List<InterventionStatistiques.ClientDepense> topClients = montants.entrySet().stream()
                // reversed : du plus grand montant au plus petit.
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> new InterventionStatistiques.ClientDepense(e.getKey(), noms.get(e.getKey()), e.getValue()))
                .toList();

        // TOP 5 DES PIECES LES PLUS UTILISEES.
        List<InterventionStatistiques.PieceUtilisee> topPieces = ligneRepository
                .piecesLesPlusUtilisees(PageRequest.of(0, 5))
                .stream()
                .map(l -> new InterventionStatistiques.PieceUtilisee((String) l[0], (String) l[1], (Long) l[2]))
                .toList();

        long sansIntervention = interventionRepository.compterVehiculesSansIntervention();

        return new InterventionStatistiques(clients, interventions, chiffreAffaires, coutMoyen,
                sansIntervention, topClients, topPieces);
    }

    // Une somme de table vide vaut null : on la remplace par 0.
    private BigDecimal ouZero(BigDecimal valeur) {
        return (valeur == null) ? BigDecimal.ZERO : valeur;
    }
}