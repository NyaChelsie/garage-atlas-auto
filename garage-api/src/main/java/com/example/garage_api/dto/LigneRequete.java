package com.example.garage_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Corps du POST /api/interventions/{id}/lignes.
// Exemple de JSON : { "pieceId": 3, "quantite": 2 }
// Pas de prix : c'est le serveur qui copie le prix actuel de la piece.
// Sinon n'importe qui pourrait facturer une piece a 1 FCFA.
public record LigneRequete(

        // NotNull : la piece doit etre indiquee.
        @NotNull(message = "La pièce est obligatoire")
        Long pieceId,

        // Integer (avec majuscule) a la place de int : il peut etre ABSENT (null).
        // NotNull attrape l'oubli, Min refuse 0 et les nombres negatifs.
        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au moins 1")
        Integer quantite
) {
}