package com.example.garage_api.entity;

// Outils JPA. Nouveaux : Table et UniqueConstraint.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Outils de validation.
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// @Table : on precise le nom de la table et une contrainte d'unicite.
// uniqueConstraints : le COUPLE (intervention_id, piece_id) ne peut apparaitre qu'une fois.
// Une meme piece ne peut donc pas figurer deux fois sur la meme facture.
// Pour en ajouter plus, on augmentera la quantite de la ligne existante.
@Entity
@Table(name = "ligne_intervention",
        uniqueConstraints = @UniqueConstraint(columnNames = {"intervention_id", "piece_id"}))
public class LigneIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PREMIERE cle etrangere : la facture a laquelle appartient cette ligne.
    // Le champ s'appelle "intervention" : c'est ce nom que repete mappedBy
    // dans la classe Intervention.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    // DEUXIEME cle etrangere : la piece utilisee.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piece_id", nullable = false)
    private Piece piece;

    // Quantite utilisee : au moins 1.
    // La regle "ne depasse pas le stock" depend de la base : elle sera verifiee dans le service.
    @NotNull(message = "La quantite est obligatoire")
    @Min(value = 1, message = "La quantite doit etre au moins 1")
    @Column(nullable = false)
    private Integer quantite;

    // Prix COPIE depuis la piece au moment de l'ajout.
    // updatable = false : une fois enregistre, JPA refuse de le modifier.
    // Ainsi, si le prix de la piece change demain, la facture d'hier reste juste.
    @NotNull(message = "Le prix unitaire est obligatoire")
    @Column(nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal prixUnitaire;

    // Getters et setters : Alt + Inser, Getter and Setter, tous les champs.


    public Long getId() {
        return id;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public Piece getPiece() {
        return piece;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }
}