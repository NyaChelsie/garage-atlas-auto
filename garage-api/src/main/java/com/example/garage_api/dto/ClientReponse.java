package com.example.garage_api.dto;

import com.example.garage_api.entity.Client;

import java.time.LocalDate;

// "Reponse" = ce que l'API RENVOIE au front. Ici, l'id est present.
public record ClientReponse(
        Long id,
        String nom,
        String prenom,
        String telephone,
        String email,
        String ville,
        LocalDate dateInscription
) {
    // static : on l'appelle sans creer d'objet, ClientReponse.depuis(client).
    // Transforme une entite Client en DTO : c'est ici qu'on choisit ce qui sort.
    public static ClientReponse depuis(Client client) {
        return new ClientReponse(
                client.getId(),
                client.getNom(),
                client.getPrenom(),
                client.getTelephone(),
                client.getEmail(),
                client.getVille(),
                client.getDateInscription());
    }
}