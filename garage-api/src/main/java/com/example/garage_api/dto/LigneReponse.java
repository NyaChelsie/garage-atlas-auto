package com.example.garage_api.dto;

import com.example.garage_api.entity.LigneIntervention;

import java.math.BigDecimal;

// Une ligne de facture, telle que le front la voit : pas l'entite, juste ce qu'il faut.
public record LigneReponse(
        Long id,
        Long pieceId,
        String pieceReference,
        String pieceDesignation,
        int quantite,
        BigDecimal prixUnitaire,
        BigDecimal sousTotal
) {
    // Convertit une entite LigneIntervention en LigneReponse.
    public static LigneReponse depuis(LigneIntervention l) {
        return new LigneReponse(
                l.getId(),
                l.getPiece().getId(),
                l.getPiece().getReference(),
                l.getPiece().getDesignation(),
                l.getQuantite(),
                // Prix COPIE a l'ajout : il ne change jamais, meme si la piece change de prix.
                l.getPrixUnitaire(),
                // Sous-total calcule : prix x quantite (BigDecimal, jamais double).
                l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite())));
    }
}