package com.example.garage_api.dto;

import com.example.garage_api.enums.StatutVehicule;

// Corps du PATCH /api/vehicules/{id}/statut.
// Exemple de JSON : { "statut": "TERMINE" }
// PATCH = modifier UNE seule information, ici le statut.
public record StatutRequete(
        StatutVehicule statut
) {
}