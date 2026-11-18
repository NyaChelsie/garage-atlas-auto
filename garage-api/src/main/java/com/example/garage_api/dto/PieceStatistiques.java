package com.example.garage_api.dto;

import com.example.garage_api.enums.CategoriePiece;

import java.math.BigDecimal;
import java.util.List;

// Les chiffres du tableau de bord pour les pieces.
public record PieceStatistiques(
        long nombreReferences,
        long quantiteTotale,
        BigDecimal valeurStock,
        long enAlerte,
        List<CategorieStat> parCategorie,
        // Peut etre null UNIQUEMENT si aucune piece n'existe (rien a designer).
        PieceChere pieceLaPlusChere
) {
    // Une categorie : combien de pieces, et la valeur de leur stock.
    public record CategorieStat(CategoriePiece categorie, long nombre, BigDecimal valeur) {
    }

    // La piece la plus chere : juste de quoi l'afficher.
    public record PieceChere(String reference, String designation, BigDecimal prixUnitaire) {
    }
}