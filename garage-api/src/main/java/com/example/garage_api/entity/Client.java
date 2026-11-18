// Adresse du fichier dans le projet.
package com.example.garage_api.entity;

// Outils JPA : transformer une classe Java en table de la base.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Outils de validation : verifier les champs avant de les enregistrer.
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

// LocalDate = une date sans heure (exemple : 2026-11-15).
import java.time.LocalDate;

// @Entity : "cette classe est une fiche du classeur" = une table dans la base.
// Le nom de la table sera "client".
@Entity
public class Client {

    // @Id : ce champ est l'identifiant unique de la fiche (la cle primaire).
    // @GeneratedValue : la base numerote toute seule (1, 2, 3...).
    // IDENTITY = on laisse la base gerer le compteur.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank : ni vide, ni fait uniquement d'espaces.
    // @Size : entre 2 et 60 caracteres. Le message s'affichera si la regle est violee.
    // @Column(nullable = false) : la colonne est obligatoire aussi dans la base.
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 60, message = "Le nom doit contenir entre 2 et 60 caracteres")
    @Column(nullable = false, length = 60)
    private String nom;

    // Meme regles pour le prenom.
    @NotBlank(message = "Le prenom est obligatoire")
    @Size(min = 2, max = 60, message = "Le prenom doit contenir entre 2 et 60 caracteres")
    @Column(nullable = false, length = 60)
    private String prenom;

    // @Pattern : le texte doit respecter une "regex" (un modele).
    // ^6\\d{8}$ se lit : commence par 6 (^6), puis exactement 8 chiffres (\\d{8}), puis fin ($).
    // Total : 9 chiffres, le premier est 6.
    // unique = true : deux clients ne peuvent pas avoir le meme telephone.
    @NotBlank(message = "Le telephone est obligatoire")
    @Pattern(regexp = "^6\\d{8}$", message = "Le telephone doit avoir 9 chiffres et commencer par 6")
    @Column(nullable = false, unique = true, length = 9)
    private String telephone;

    // Facultatif : pas de @NotBlank. @Email verifie le format s'il est rempli.
    // Pas de nullable = false : la colonne peut rester vide.
    @Email(message = "Format d'e-mail invalide")
    private String email;

    // Facultative, 60 caracteres maximum.
    @Size(max = 60, message = "La ville ne doit pas depasser 60 caracteres")
    @Column(length = 60)
    private String ville;

    // @NotNull : obligatoire. @PastOrPresent : aujourd'hui ou dans le passe, jamais dans le futur.
    @NotNull(message = "La date d'inscription est obligatoire")
    @PastOrPresent(message = "La date d'inscription ne peut pas etre dans le futur")
    @Column(nullable = false)
    private LocalDate dateInscription;

    // Les getters et setters (les "portes" pour lire et ecrire chaque champ)
    // se generent avec IntelliJ : voir l'etape 4 ci-dessous.

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getVille() {
        return ville;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }
}