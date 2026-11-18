package com.example.garage_api.controller;

import com.example.garage_api.dto.ClientReponse;
import com.example.garage_api.dto.ClientRequete;
import com.example.garage_api.dto.PageReponse;
import com.example.garage_api.entity.Client;
import com.example.garage_api.service.ClientService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// @RestController : cette classe repond aux requetes HTTP et renvoie du JSON.
// @RequestMapping : toutes les adresses de cette classe commencent par /api/clients.
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    // Le controleur ne contient AUCUNE regle : il passe la main au service.
    private final ClientService clientService;

    // Constructeur : Spring fournit lui-meme le service (injection de dependances).
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // GET /api/clients?page=0&taille=10&recherche=ab  -> 200
    // @RequestParam : lit un parametre de l'adresse ; defaultValue = valeur si absent.
    @GetMapping
    public PageReponse<ClientReponse> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) String recherche) {
        // PageRequest.of(page, taille, tri) : la demande de pagination, triee par id.
        Page<Client> resultat = clientService.lister(recherche, PageRequest.of(page, taille, Sort.by("id")));
        // On convertit chaque Client en ClientReponse : l'entite ne sort jamais.
        return PageReponse.de(resultat, ClientReponse::depuis);
    }

    // GET /api/clients/3  -> 200
    // @PathVariable : lit l'id place dans l'adresse.
    @GetMapping("/{id}")
    public ClientReponse lire(@PathVariable Long id) {
        return ClientReponse.depuis(clientService.lire(id));
    }

    // POST /api/clients  -> 201
    // @RequestBody : transforme le JSON recu en ClientRequete.
    // @ResponseStatus : le code HTTP renvoye si tout va bien (201 = Created).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientReponse creer(@Valid @RequestBody ClientRequete requete) {
        return ClientReponse.depuis(clientService.creer(requete.versEntite()));
    }

    @PutMapping("/{id}")
    public ClientReponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequete requete) {
        return ClientReponse.depuis(clientService.modifier(id, requete.versEntite()));
    }

    // DELETE /api/clients/3  -> 204 (No Content : succes, rien a renvoyer)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        clientService.supprimer(id);
    }
}