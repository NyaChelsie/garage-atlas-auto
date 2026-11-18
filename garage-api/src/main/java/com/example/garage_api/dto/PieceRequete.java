package com.example.garage_api.dto;

import com.example.garage_api.entity.Piece;
import com.example.garage_api.enums.CategoriePiece;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PieceRequete(

        @NotBlank(message = "La reference est obligatoire")
        @Pattern(regexp = "^PCE-\\d{4}$", message = "Format de reference attendu : PCE-0001")
        String reference,

        @NotBlank(message = "La designation est obligatoire")
        @Size(min = 3, max = 80, message = "La designation doit contenir entre 3 et 80 caracteres")
        String designation,

        @NotNull(message = "La categorie est obligatoire")
        CategoriePiece categorie,

        @NotBlank(message = "Le fabricant est obligatoire")
        @Size(max = 60, message = "Le fabricant ne peut pas depasser 60 caracteres")
        String fabricant,

        @NotNull(message = "Le prix unitaire est obligatoire")
        @DecimalMin(value = "0.01", inclusive = true, message = "Le prix unitaire doit etre strictement superieur a 0")
        BigDecimal prixUnitaire,

        @NotNull(message = "La quantite en stock est obligatoire")
        @Min(value = 0, message = "La quantite en stock ne peut pas etre negative")
        Integer quantiteStock,

        @NotNull(message = "Le seuil d'alerte est obligatoire")
        @Min(value = 0, message = "Le seuil d'alerte ne peut pas etre negatif")
        Integer seuilAlerte,

        @NotNull(message = "La date d'achat est obligatoire")
        @PastOrPresent(message = "La date d'achat ne peut pas etre dans le futur")
        LocalDate dateAchat
) {
    public Piece versEntite() {
        Piece piece = new Piece();
        piece.setReference(reference);
        piece.setDesignation(designation);
        piece.setCategorie(categorie);
        piece.setFabricant(fabricant);
        piece.setPrixUnitaire(prixUnitaire);
        piece.setQuantiteStock(quantiteStock);
        piece.setSeuilAlerte(seuilAlerte);
        piece.setDateAchat(dateAchat);
        return piece;
    }
}