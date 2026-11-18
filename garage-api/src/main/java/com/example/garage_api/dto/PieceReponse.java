package com.example.garage_api.dto;

import com.example.garage_api.entity.Piece;
import com.example.garage_api.enums.CategoriePiece;

import java.math.BigDecimal;
import java.time.LocalDate;

// "Reponse" = ce que l'API RENVOIE au front. Ici, l'id est present.
// On ajoute un champ en plus, "enAlerte" : le serveur fait le calcul,
// le front n'a plus qu'a l'afficher (surlignage des pieces a reapprovisionner).
public record PieceReponse(
        Long id,
        String reference,
        String designation,
        CategoriePiece categorie,
        String fabricant,
        BigDecimal prixUnitaire,
        int quantiteStock,
        int seuilAlerte,
        LocalDate dateAchat,
        boolean enAlerte
) {
    // static : on l'appelle sans creer d'objet, PieceReponse.depuis(piece).
    // Transforme une entite Piece en DTO : c'est ici qu'on choisit ce qui sort.
    public static PieceReponse depuis(Piece piece) {
        return new PieceReponse(
                piece.getId(),
                piece.getReference(),
                piece.getDesignation(),
                piece.getCategorie(),
                piece.getFabricant(),
                piece.getPrixUnitaire(),
                piece.getQuantiteStock(),
                piece.getSeuilAlerte(),
                piece.getDateAchat(),
                // Regle du sujet : alerte quand quantite <= seuil.
                piece.getQuantiteStock() <= piece.getSeuilAlerte());
    }
}