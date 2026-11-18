package com.example.garage_api.controller;

import com.example.garage_api.dto.PageReponse;
import com.example.garage_api.dto.StatutRequete;
import com.example.garage_api.dto.VehiculeReponse;
import com.example.garage_api.dto.VehiculeRequete;
import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.service.VehiculeService;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Prefixe /api seulement (et non /api/vehicules) : cette classe porte aussi
// la route /api/clients/{id}/vehicules, qui ne commence pas par /api/vehicules.
@RestController
@RequestMapping("/api")
public class VehiculeController {

    // Le controleur ne contient AUCUNE regle : il passe la main au service.
    private final VehiculeService vehiculeService;

    // Constructeur : Spring fournit lui-meme le service (injection de dependances).
    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    // GET /api/vehicules?page=0&taille=10&recherche=toyota&statut=EN_ATTENTE&clientId=1  -> 200
    // required = false : le parametre est facultatif ; absent, il vaut null et le filtre est saute.
    // Spring convertit tout seul le texte "EN_ATTENTE" en valeur de l'enum StatutVehicule.
    @GetMapping("/vehicules")
    public PageReponse<VehiculeReponse> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) StatutVehicule statut,
            @RequestParam(required = false) Long clientId) {
        Page<Vehicule> resultat = vehiculeService.lister(
                recherche, statut, clientId, PageRequest.of(page, taille, Sort.by("id")));
        // Chaque Vehicule devient un VehiculeReponse : l'entite ne sort jamais.
        return PageReponse.de(resultat, VehiculeReponse::depuis);
    }

    // GET /api/vehicules/3  -> 200
    @GetMapping("/vehicules/{id}")
    public VehiculeReponse lire(@PathVariable Long id) {
        return VehiculeReponse.depuis(vehiculeService.lire(id));
    }

    // POST /api/vehicules  -> 201
    // Le clientId est lu dans le corps de la requete et passe a part au service.
    @PostMapping("/vehicules")
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculeReponse creer(@Valid @RequestBody VehiculeRequete requete) {
        return VehiculeReponse.depuis(
                vehiculeService.creer(requete.versEntite(), requete.clientId()));
    }

    @PutMapping("/vehicules/{id}")
    public VehiculeReponse modifier(@PathVariable Long id, @Valid @RequestBody VehiculeRequete requete) {
        return VehiculeReponse.depuis(
                vehiculeService.modifier(id, requete.versEntite(), requete.clientId()));
    }

    @PatchMapping("/vehicules/{id}/statut")
    public VehiculeReponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequete requete) {
        return VehiculeReponse.depuis(vehiculeService.changerStatut(id, requete.statut()));
    }

    // DELETE /api/vehicules/3  -> 204 (succes, rien a renvoyer)
    @DeleteMapping("/vehicules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        vehiculeService.supprimer(id);
    }

    // GET /api/clients/1/vehicules  -> 200 : tous les vehicules d'un client (liste simple).
    @GetMapping("/clients/{id}/vehicules")
    public List<VehiculeReponse> duClient(@PathVariable Long id) {
        return vehiculeService.listerParClient(id).stream()
                .map(VehiculeReponse::depuis).toList();
    }
}