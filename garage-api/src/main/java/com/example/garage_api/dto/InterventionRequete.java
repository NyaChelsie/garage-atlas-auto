package com.example.garage_api.dto;

import com.example.garage_api.entity.Intervention;
import com.example.garage_api.enums.TypeIntervention;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// Ce que le front envoie pour creer (POST) ou modifier (PUT) une intervention.
// vehiculeId sert a la creation ; a la modification il est ignore par le service
// (l'intervention reste attachee a son vehicule), mais il doit quand meme etre envoye.
public record InterventionRequete(

        @NotNull(message = "Le véhicule est obligatoire")
        Long vehiculeId,

        @NotNull(message = "Le type d'intervention est obligatoire")
        TypeIntervention type,

        @NotBlank(message = "La description est obligatoire")
        @Size(min = 5, max = 300, message = "La description doit contenir entre 5 et 300 caractères")
        String description,

        @NotNull(message = "La date de début est obligatoire")
        @PastOrPresent(message = "La date de début ne peut pas être dans le futur")
        LocalDate dateDebut,

        // Facultative : pas d'annotation ici. Sa regle (pas avant le debut) compare DEUX champs,
        // elle est donc ecrite plus bas, dans la methode isDatesCoherentes.
        LocalDate dateFin,

        @DecimalMin(value = "0.0", message = "La main-d'œuvre ne peut pas être négative")
        BigDecimal mainOeuvre
) {

    // AssertTrue : la regle est respectee quand cette methode renvoie true.
    // Elle est vraie si la fin est absente, ou si elle n'est pas avant le debut.
    // Si une des deux dates manque, on laisse les autres regles s'exprimer.
    // Le nom du champ dans la reponse 400 sera "datesCoherentes".
    @AssertTrue(message = "La date de fin ne peut pas être avant la date de début")
    public boolean isDatesCoherentes() {
        return dateFin == null || dateDebut == null || !dateFin.isBefore(dateDebut);
    }

    // Entite SANS vehicule ni lignes : le service s'en occupe.
    public Intervention versEntite() {
        Intervention i = new Intervention();
        i.setType(type);
        i.setDescription(description);
        i.setDateDebut(dateDebut);
        i.setDateFin(dateFin);
        i.setMainOeuvre(mainOeuvre);
        return i;
    }
}