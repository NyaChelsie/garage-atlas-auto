package com.example.garage_api.controller;

import com.example.garage_api.dto.InterventionReponse;
import com.example.garage_api.dto.InterventionRequete;
import com.example.garage_api.dto.LigneRequete;
import com.example.garage_api.service.InterventionService;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @GetMapping("/vehicules/{id}/interventions")
    public List<InterventionReponse> historique(@PathVariable Long id) {
        return interventionService.listerParVehicule(id).stream()
                .map(InterventionReponse::depuis).toList();
    }

    @GetMapping("/interventions/{id}")
    public InterventionReponse lire(@PathVariable Long id) {
        return InterventionReponse.depuis(interventionService.lire(id));
    }

    @PostMapping("/interventions")
    @ResponseStatus(HttpStatus.CREATED)
    public InterventionReponse creer(@Valid @RequestBody InterventionRequete requete) {
        return InterventionReponse.depuis(
                interventionService.creer(requete.versEntite(), requete.vehiculeId()));
    }

    @PutMapping("/interventions/{id}")
    public InterventionReponse modifier(@PathVariable Long id,
                                        @Valid @RequestBody InterventionRequete requete) {
        return InterventionReponse.depuis(
                interventionService.modifier(id, requete.versEntite()));
    }

    @DeleteMapping("/interventions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        interventionService.supprimer(id);
    }

    @PostMapping("/interventions/{id}/lignes")
    @ResponseStatus(HttpStatus.CREATED)
    public InterventionReponse ajouterLigne(@PathVariable Long id,
                                            @Valid @RequestBody LigneRequete requete) {
        return InterventionReponse.depuis(
                interventionService.ajouterLigne(id, requete.pieceId(), requete.quantite()));
    }

    @DeleteMapping("/interventions/{id}/lignes/{ligneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        interventionService.retirerLigne(id, ligneId);
    }
}