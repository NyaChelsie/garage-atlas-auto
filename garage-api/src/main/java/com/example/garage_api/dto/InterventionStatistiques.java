package com.example.garage_api.dto;

import java.math.BigDecimal;
import java.util.List;

// Les chiffres du tableau de bord pour les clients et les interventions.
public record InterventionStatistiques(
        long nombreClients,
        long nombreInterventions,
        BigDecimal chiffreAffairesMois,
        BigDecimal coutMoyenIntervention,
        long vehiculesSansIntervention,
        List<ClientDepense> topClients,
        List<PieceUtilisee> topPieces
) {
    // Un client et le montant total qu'il a depense (main-d'oeuvre + pieces).
    public record ClientDepense(Long clientId, String nom, BigDecimal montant) {
    }

    // Une piece et la quantite totale utilisee dans toutes les interventions.
    public record PieceUtilisee(String reference, String designation, long quantite) {
    }
}