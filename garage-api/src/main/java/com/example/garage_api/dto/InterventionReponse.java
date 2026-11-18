package com.example.garage_api.dto;

import com.example.garage_api.entity.Intervention;
import com.example.garage_api.enums.TypeIntervention;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Ce que l'API renvoie : l'intervention, ses lignes et son total.
// Pas de vehicule entier (pas de boucle infinie) : seulement vehiculeId.
public record InterventionReponse(
        Long id,
        Long vehiculeId,
        TypeIntervention type,
        String description,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal mainOeuvre,
        List<LigneReponse> lignes,
        BigDecimal total
) {
    // Convertit une entite Intervention en InterventionReponse.
    public static InterventionReponse depuis(Intervention i) {
        return new InterventionReponse(
                i.getId(),
                i.getVehicule().getId(),
                i.getType(),
                i.getDescription(),
                i.getDateDebut(),
                i.getDateFin(),
                i.getMainOeuvre(),
                // Chaque LigneIntervention devient une LigneReponse.
                i.getLignes().stream().map(LigneReponse::depuis).toList(),
                // getTotal() est calcule dans l'entite : mainOeuvre + somme des lignes.
                i.getTotal());
    }
}