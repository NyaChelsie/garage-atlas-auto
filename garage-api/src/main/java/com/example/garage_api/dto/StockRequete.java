package com.example.garage_api.dto;

import com.example.garage_api.enums.OperationStock;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Corps de la requete PATCH /api/pieces/{id}/stock
// Exemple : { "operation": "SORTIE", "quantite": 2 }
public record StockRequete(

        @NotNull(message = "L'operation de stock est obligatoire (ENTREE ou SORTIE)")
        OperationStock operation,

        // Integer (et non int) : le champ peut etre ABSENT, donc NotNull peut le reperer
        // et renvoyer un message precis au lieu de "corps illisible".
        @NotNull(message = "La quantite est obligatoire")
        @Min(value = 1, message = "La quantite doit etre au moins egale a 1")
        Integer quantite
) {
}