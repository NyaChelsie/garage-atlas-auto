package com.example.garage_api.dto;

import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.StatutVehicule;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

// Ce que le front envoie pour creer (POST) ou modifier (PUT) un vehicule.
// Pas d'id : il est genere par la base. Le proprietaire est designe par clientId.
// Chaque annotation est une REGLE : si elle n'est pas respectee, @Valid (dans le controleur)
// bloque la requete et le gestionnaire d'erreurs renvoie un 400 avec le message du champ.
public record VehiculeRequete(

        @NotNull(message = "Le client est obligatoire")
        Long clientId,

        @NotBlank(message = "L'immatriculation est obligatoire")
        @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$",
                message = "L'immatriculation doit respecter le format AB-123-CD")
        String immatriculation,

        @NotBlank(message = "La marque est obligatoire")
        @Size(min = 2, max = 40, message = "La marque doit contenir entre 2 et 40 caractères")
        String marque,

        @NotBlank(message = "Le modèle est obligatoire")
        @Size(min = 2, max = 40, message = "Le modèle doit contenir entre 2 et 40 caractères")
        String modele,

        // Min : borne basse FIXE (2000). La borne haute (annee en cours) change chaque annee :
        // une annotation ne peut pas la calculer, elle est donc ecrite plus bas (isAnneeCoherente).
        @Min(value = 2000, message = "L'année doit être au moins 2000")
        Integer annee,

        @Min(value = 0, message = "Le kilométrage ne peut pas être négatif")
        Integer kilometrage,

        @NotNull(message = "Le carburant est obligatoire")
        Carburant carburant,

        // Le statut n'est pas controle : le service impose EN_ATTENTE a la creation.
        StatutVehicule statut,

        @NotNull(message = "La date d'entrée est obligatoire")
        @PastOrPresent(message = "La date d'entrée ne peut pas être dans le futur")
        LocalDate dateEntree,

        @DecimalMin(value = "0.0", message = "Le coût estimé ne peut pas être négatif")
        BigDecimal coutEstime,

        @Size(max = 500, message = "La panne ne peut pas dépasser 500 caractères")
        String panne
) {

    // AssertTrue : la regle est respectee quand cette methode renvoie true.
    // Year.now().getValue() : l'annee en cours, lue sur l'horloge a CHAQUE requete
    // (rien n'est ecrit en dur : en 2027 la regle suit toute seule).
    // Si l'annee est absente, on laisse les autres regles s'exprimer.
    // Le nom du champ dans la reponse 400 sera "anneeCoherente".
    @AssertTrue(message = "L'année ne peut pas dépasser l'année en cours")
    public boolean isAnneeCoherente() {
        return annee == null || annee <= Year.now().getValue();
    }

    // Entite SANS client : c'est le service qui retrouve le client grace a clientId.
    public Vehicule versEntite() {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immatriculation);
        v.setMarque(marque);
        v.setModele(modele);
        v.setAnnee(annee);
        v.setKilometrage(kilometrage);
        v.setCarburant(carburant);
        v.setStatut(statut);
        v.setDateEntree(dateEntree);
        v.setCoutEstime(coutEstime);
        v.setPanne(panne);
        return v;
    }
}