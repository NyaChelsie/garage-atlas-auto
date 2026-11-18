package com.example.garage_api.controller;

import com.example.garage_api.dto.InterventionStatistiques;
import com.example.garage_api.dto.PieceStatistiques;
import com.example.garage_api.dto.VehiculeStatistiques;
import com.example.garage_api.service.StatistiquesInterventionService;
import com.example.garage_api.service.StatistiquesService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;
    private final StatistiquesInterventionService statistiquesInterventionService;

    public StatistiquesController(StatistiquesService statistiquesService,
                                  StatistiquesInterventionService statistiquesInterventionService) {
        this.statistiquesService = statistiquesService;
        this.statistiquesInterventionService = statistiquesInterventionService;
    }

    // GET /api/vehicules/statistiques -> 200
    @GetMapping("/vehicules/statistiques")
    public VehiculeStatistiques vehicules() {
        return statistiquesService.statistiquesVehicules();
    }

    // GET /api/pieces/statistiques -> 200
    @GetMapping("/pieces/statistiques")
    public PieceStatistiques pieces() {
        return statistiquesService.statistiquesPieces();
    }

    // GET /api/interventions/statistiques -> 200
    @GetMapping("/interventions/statistiques")
    public InterventionStatistiques interventions() {
        return statistiquesInterventionService.statistiques();
    }
}