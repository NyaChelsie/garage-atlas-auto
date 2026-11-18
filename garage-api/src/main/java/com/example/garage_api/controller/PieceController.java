package com.example.garage_api.controller;

import com.example.garage_api.dto.PageReponse;
import com.example.garage_api.dto.PieceReponse;
import com.example.garage_api.dto.PieceRequete;
import com.example.garage_api.dto.StockRequete;
import com.example.garage_api.entity.Piece;
import com.example.garage_api.enums.CategoriePiece;
import com.example.garage_api.service.PieceService;

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

// @RestController : cette classe repond aux requetes HTTP et renvoie du JSON.
// @RequestMapping : toutes les adresses de cette classe commencent par /api/pieces.
@RestController
@RequestMapping("/api/pieces")
public class PieceController {

    // Le controleur ne contient AUCUNE regle : il passe la main au service.
    private final PieceService pieceService;

    // Constructeur : Spring fournit lui-meme le service (injection de dependances).
    public PieceController(PieceService pieceService) {
        this.pieceService = pieceService;
    }

    // GET /api/pieces?page=0&taille=10&recherche=frein&categorie=FREINAGE  -> 200
    // recherche et categorie sont facultatives (required = false).
    // Spring convertit tout seul le texte "FREINAGE" en valeur de l'enum CategoriePiece.
    @GetMapping
    public PageReponse<PieceReponse> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) CategoriePiece categorie) {
        Page<Piece> resultat = pieceService.lister(recherche, categorie,
                PageRequest.of(page, taille, Sort.by("id")));
        // On convertit chaque Piece en PieceReponse : l'entite ne sort jamais.
        return PageReponse.de(resultat, PieceReponse::depuis);
    }

    // GET /api/pieces/alertes  -> 200 : les pieces dont la quantite est <= au seuil.
    // Cette adresse est litterale (/alertes) : Spring la prefere a /{id},
    // donc "alertes" n'est jamais pris pour un identifiant.
    @GetMapping("/alertes")
    public List<PieceReponse> alertes() {
        return pieceService.listerAlertes().stream()
                .map(PieceReponse::depuis).toList();
    }

    // GET /api/pieces/3  -> 200
    @GetMapping("/{id}")
    public PieceReponse lire(@PathVariable Long id) {
        return PieceReponse.depuis(pieceService.lire(id));
    }

    // POST /api/pieces  -> 201 (Created)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PieceReponse creer(@Valid @RequestBody PieceRequete requete) {
        return PieceReponse.depuis(pieceService.creer(requete.versEntite()));
    }

    @PutMapping("/{id}")
    public PieceReponse modifier(@PathVariable Long id, @Valid @RequestBody PieceRequete requete) {
        return PieceReponse.depuis(pieceService.modifier(id, requete.versEntite()));
    }

    @PatchMapping("/{id}/stock")
    public PieceReponse mouvementStock(@PathVariable Long id, @Valid @RequestBody StockRequete requete) {
        Piece piece = switch (requete.operation()) {
            case ENTREE -> pieceService.entrerStock(id, requete.quantite());
            case SORTIE -> pieceService.sortirStock(id, requete.quantite());
        };
        return PieceReponse.depuis(piece);
    }

    // DELETE /api/pieces/3  -> 204 (No Content : succes, rien a renvoyer)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        pieceService.supprimer(id);
    }
}