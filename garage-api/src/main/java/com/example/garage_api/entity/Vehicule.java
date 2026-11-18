package com.example.garage_api.entity;

// Outils JPA. Nouveaux par rapport a Client : Enumerated, FetchType, JoinColumn, ManyToOne.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Outils de validation.
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Nos enums, crees au temps A.
import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.StatutVehicule;

// BigDecimal = nombre decimal exact, obligatoire pour l'argent (jamais double).
import java.math.BigDecimal;
import java.time.LocalDate;

// Cette classe devient la table "vehicule".
@Entity
public class Vehicule {

    // Cle primaire numerotee automatiquement par la base.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Format AB-123-CD : 2 lettres majuscules, tiret, 3 chiffres, tiret, 2 lettres majuscules.
    // [A-Z]{2} = exactement 2 lettres de A a Z. \\d{3} = exactement 3 chiffres.
    // unique = true : deux vehicules ne peuvent pas avoir la meme plaque.
    @NotBlank(message = "L'immatriculation est obligatoire")
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$",
            message = "Format attendu : AB-123-CD")
    @Column(nullable = false, unique = true, length = 9)
    private String immatriculation;

    // Marque et modele : obligatoires, de 2 a 40 caracteres.
    @NotBlank(message = "La marque est obligatoire")
    @Size(min = 2, max = 40, message = "La marque doit contenir entre 2 et 40 caracteres")
    @Column(nullable = false, length = 40)
    private String marque;

    @NotBlank(message = "Le modele est obligatoire")
    @Size(min = 2, max = 40, message = "Le modele doit contenir entre 2 et 40 caracteres")
    @Column(nullable = false, length = 40)
    private String modele;

    // @Min(2000) : valeur minimale. La borne haute (annee en cours) sera verifiee dans le service.
    // Integer (et non int) : peut valoir null, ce qui permet de detecter un champ non rempli.
    @Min(value = 2000, message = "L'annee doit etre superieure ou egale a 2000")
    private Integer annee;

    // Le kilometrage ne peut pas etre negatif.
    @Min(value = 0, message = "Le kilometrage ne peut pas etre negatif")
    private Integer kilometrage;

    // @Enumerated(EnumType.STRING) : on enregistre le NOM ("DIESEL") et non le numero (1).
    // Si on reordonne l'enum plus tard, les donnees restent justes.
    @NotNull(message = "Le carburant est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Carburant carburant;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutVehicule statut;

    // Date d'arrivee au garage : obligatoire, aujourd'hui ou avant.
    @NotNull(message = "La date d'entree est obligatoire")
    @PastOrPresent(message = "La date d'entree ne peut pas etre dans le futur")
    @Column(nullable = false)
    private LocalDate dateEntree;

    // Argent : BigDecimal. precision = 12 chiffres au total, scale = 2 apres la virgule.
    // @DecimalMin("0.0") : jamais negatif.
    @DecimalMin(value = "0.0", message = "Le cout estime ne peut pas etre negatif")
    @Column(precision = 12, scale = 2)
    private BigDecimal coutEstime;

    // Description de la panne : facultative, 500 caracteres maximum.
    @Size(max = 500, message = "La panne ne doit pas depasser 500 caracteres")
    @Column(length = 500)
    private String panne;

    // RELATION : plusieurs vehicules pour un client.
    // @ManyToOne : "plusieurs (ce vehicule) vers un (son client)".
    // fetch = LAZY : le client n'est charge que si on le demande (plus rapide).
    // optional = false : un vehicule doit toujours avoir un client.
    // @JoinColumn(name = "client_id") : la colonne de cle etrangere dans la table vehicule.
    // Pas de "cascade" : supprimer un client ne doit JAMAIS supprimer ses vehicules.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Getters et setters : a generer avec IntelliJ (Alt + Inser).


    public Long getId() {
        return id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public String getModele() {
        return modele;
    }

    public Integer getAnnee() {
        return annee;
    }

    public Integer getKilometrage() {
        return kilometrage;
    }

    public Carburant getCarburant() {
        return carburant;
    }

    public StatutVehicule getStatut() {
        return statut;
    }

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    public BigDecimal getCoutEstime() {
        return coutEstime;
    }

    public String getPanne() {
        return panne;
    }

    public Client getClient() {
        return client;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public void setKilometrage(Integer kilometrage) {
        this.kilometrage = kilometrage;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public void setCarburant(Carburant carburant) {
        this.carburant = carburant;
    }

    public void setStatut(StatutVehicule statut) {
        this.statut = statut;
    }

    public void setDateEntree(LocalDate dateEntree) {
        this.dateEntree = dateEntree;
    }

    public void setCoutEstime(BigDecimal coutEstime) {
        this.coutEstime = coutEstime;
    }

    public void setPanne(String panne) {
        this.panne = panne;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}