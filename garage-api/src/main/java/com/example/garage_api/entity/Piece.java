package com.example.garage_api.entity;

// Outils JPA.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Outils de validation.
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Notre enum des categories de pieces.
import com.example.garage_api.enums.CategoriePiece;

import java.math.BigDecimal;
import java.time.LocalDate;

// Cette classe devient la table "piece".
@Entity
public class Piece {

    // Cle primaire numerotee automatiquement par la base.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Format PCE-0001 : les lettres PCE, un tiret, puis exactement 4 chiffres.
    // unique = true : deux pieces ne peuvent pas avoir la meme reference.
    @NotBlank(message = "La reference est obligatoire")
    @Pattern(regexp = "^PCE-\\d{4}$", message = "Format attendu : PCE-0001")
    @Column(nullable = false, unique = true, length = 8)
    private String reference;

    // Designation : le nom de la piece, de 3 a 80 caracteres.
    @NotBlank(message = "La designation est obligatoire")
    @Size(min = 3, max = 80, message = "La designation doit contenir entre 3 et 80 caracteres")
    @Column(nullable = false, length = 80)
    private String designation;

    // Categorie enregistree par son NOM (EnumType.STRING), pas par son numero.
    @NotNull(message = "La categorie est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriePiece categorie;

    // Fabricant : obligatoire, sans limite de longueur dans le sujet.
    @NotBlank(message = "Le fabricant est obligatoire")
    @Column(nullable = false)
    private String fabricant;

    // Prix : "strictement superieur a 0".
    // inclusive = false : la valeur 0 elle-meme est refusee (seulement > 0).
    // @NotNull : indispensable, car @DecimalMin laisse passer un champ vide.
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit etre strictement superieur a 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    // Quantite en stock : jamais negative.
    @NotNull(message = "La quantite en stock est obligatoire")
    @Min(value = 0, message = "La quantite en stock ne peut pas etre negative")
    @Column(nullable = false)
    private Integer quantiteStock;

    // Seuil d'alerte : quand quantiteStock <= seuilAlerte, la piece est "en alerte".
    @NotNull(message = "Le seuil d'alerte est obligatoire")
    @Min(value = 0, message = "Le seuil d'alerte ne peut pas etre negatif")
    @Column(nullable = false)
    private Integer seuilAlerte;

    // Date d'achat : obligatoire, aujourd'hui ou avant.
    @NotNull(message = "La date d'achat est obligatoire")
    @PastOrPresent(message = "La date d'achat ne peut pas etre dans le futur")
    @Column(nullable = false)
    private LocalDate dateAchat;

    // Getters et setters : Alt + Inser, Getter and Setter, tous les champs.


    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getDesignation() {
        return designation;
    }

    public CategoriePiece getCategorie() {
        return categorie;
    }

    public String getFabricant() {
        return fabricant;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public Integer getQuantiteStock() {
        return quantiteStock;
    }

    public Integer getSeuilAlerte() {
        return seuilAlerte;
    }

    public LocalDate getDateAchat() {
        return dateAchat;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setCategorie(CategoriePiece categorie) {
        this.categorie = categorie;
    }

    public void setFabricant(String fabricant) {
        this.fabricant = fabricant;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setQuantiteStock(Integer quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public void setSeuilAlerte(Integer seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public void setDateAchat(LocalDate dateAchat) {
        this.dateAchat = dateAchat;
    }
}