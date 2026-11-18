package com.example.garage_api.dto;

import com.example.garage_api.entity.Client;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

// record : une classe courte, faite pour transporter des donnees.
// "Requete" = ce que le front ENVOIE a l'API (POST et PUT). Pas d'id.
public record ClientRequete(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 60, message = "Le nom doit contenir entre 2 et 60 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 60, message = "Le prénom doit contenir entre 2 et 60 caractères")
        String prenom,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^6\\d{8}$", message = "Le téléphone doit contenir 9 chiffres et commencer par 6")
        String telephone,

        // Facultatif : on autorise null ou chaine vide, mais si present -> format email
        @Email(message = "Format d'email invalide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        String email,

        @Size(max = 60, message = "La ville ne peut pas dépasser 60 caractères")
        String ville,

        @NotNull(message = "La date d'inscription est obligatoire")
        @PastOrPresent(message = "La date d'inscription ne peut pas être dans le futur")
        LocalDate dateInscription
) {
    // Transforme la requete en entite Client, pour la donner au service.
    public Client versEntite() {
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setTelephone(telephone);
        client.setEmail(email);
        client.setVille(ville);
        client.setDateInscription(dateInscription);
        return client;
    }
}