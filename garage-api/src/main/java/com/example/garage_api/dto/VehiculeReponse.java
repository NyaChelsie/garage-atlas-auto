package com.example.garage_api.dto;

import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.StatutVehicule;

import java.math.BigDecimal;
import java.time.LocalDate;

// Ce que l'API renvoie pour un vehicule.
// On ne renvoie PAS le client entier (sinon boucle infinie dans le JSON) :
// seulement son identifiant et son nom, comme le demande le sujet.
public record VehiculeReponse(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        Integer annee,
        Integer kilometrage,
        Carburant carburant,
        StatutVehicule statut,
        LocalDate dateEntree,
        BigDecimal coutEstime,
        String panne,
        Long clientId,
        String clientNom
) {
    // Convertit une entite Vehicule en VehiculeReponse.
    public static VehiculeReponse depuis(Vehicule v) {
        return new VehiculeReponse(
                v.getId(),
                v.getImmatriculation(),
                v.getMarque(),
                v.getModele(),
                v.getAnnee(),
                v.getKilometrage(),
                v.getCarburant(),
                v.getStatut(),
                v.getDateEntree(),
                v.getCoutEstime(),
                v.getPanne(),
                // Le client est charge en LAZY : on ne lit que ce dont on a besoin.
                v.getClient().getId(),
                v.getClient().getPrenom() + " " + v.getClient().getNom());
    }
}