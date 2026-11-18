package com.example.garage_api.dto;

import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.StatutVehicule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Les chiffres du tableau de bord pour les vehicules.
// long (et non Long) : un nombre qui n'est jamais absent. Base vide = 0.
public record VehiculeStatistiques(
        long total,
        Map<StatutVehicule, Long> parStatut,
        Map<Carburant, Long> parCarburant,
        List<MarqueCompte> topMarques,
        double kilometrageMoyen,
        BigDecimal coutEstimeEnReparation,
        long entresCeMois
) {
    // Un record peut en contenir un autre : une marque et son nombre de vehicules.
    public record MarqueCompte(String marque, long nombre) {
    }
}