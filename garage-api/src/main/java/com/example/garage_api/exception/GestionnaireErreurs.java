package com.example.garage_api.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// @RestControllerAdvice : "ce guichet intercepte les exceptions de TOUS les controleurs
// et renvoie du JSON". Un seul endroit pour toutes les erreurs de l'API.
@RestControllerAdvice
public class GestionnaireErreurs {

    // Le journal du serveur : la trace technique va ICI (console IntelliJ), jamais dans la reponse.
    private static final Logger JOURNAL = LoggerFactory.getLogger(GestionnaireErreurs.class);

    // ---- 404 : introuvable ----

    // @ExceptionHandler : "quand cette exception sort d'un controleur, execute cette methode".
    @ExceptionHandler(IntrouvableException.class)
    public ResponseEntity<ErreurReponse> introuvable(IntrouvableException ex) {
        // Le message vient de notre service, par exemple "Vehicule introuvable : 9999".
        return construire(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    // Une adresse qui n'existe pas du tout (exemple : /api/inconnu).
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErreurReponse> adresseInconnue(NoResourceFoundException ex) {
        return construire(HttpStatus.NOT_FOUND, "Adresse introuvable.", Map.of());
    }

    // ---- 409 : conflit ----

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ErreurReponse> conflit(ConflitException ex) {
        return construire(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    // Filet de securite : la base refuse une donnee (doublon, cle etrangere...)
    // alors que nos controles du service ne l'ont pas vue.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurReponse> integrite(DataIntegrityViolationException ex) {
        JOURNAL.warn("Violation d'integrite en base", ex);
        return construire(HttpStatus.CONFLICT,
                "Opération impossible : elle entre en conflit avec des données existantes.", Map.of());
    }

    // ---- 400 : requete invalide ----

    // Les contraintes des DTO ne sont pas respectees (@NotBlank, @Size...).
    // On construit la liste "champ -> message" demandee par le sujet.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurReponse> validation(MethodArgumentNotValidException ex) {
        // LinkedHashMap : garde l'ordre des champs.
        Map<String, String> erreurs = new LinkedHashMap<>();
        for (FieldError erreur : ex.getBindingResult().getFieldErrors()) {
            // putIfAbsent : si un champ a plusieurs erreurs, on garde la premiere.
            erreurs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage());
        }
        return construire(HttpStatus.BAD_REQUEST, "Données invalides", erreurs);
    }

    // Une regle metier de nos services (exemple : quantite < 1, fin avant le debut).
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErreurReponse> argumentInvalide(IllegalArgumentException ex) {
        return construire(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    // Le corps de la requete est illisible : JSON mal forme, date incorrecte,
    // valeur d'enum inconnue (exemple : "carburant": "EAU").
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurReponse> corpsIllisible(HttpMessageNotReadableException ex) {
        return construire(HttpStatus.BAD_REQUEST,
                "Corps de la requête illisible : JSON mal formé, date ou valeur incorrecte.", Map.of());
    }

    // Un parametre de l'adresse n'a pas le bon type (exemple : /api/vehicules/abc
    // ou ?statut=XYZ).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurReponse> parametreInvalide(MethodArgumentTypeMismatchException ex) {
        return construire(HttpStatus.BAD_REQUEST,
                "Valeur invalide pour le paramètre : " + ex.getName(), Map.of());
    }

    // ---- Autres erreurs HTTP courantes ----

    // Mauvaise methode (exemple : DELETE sur /api/pieces/alertes).
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErreurReponse> methodeInterdite(HttpRequestMethodNotSupportedException ex) {
        return construire(HttpStatus.METHOD_NOT_ALLOWED, "Méthode HTTP non autorisée.", Map.of());
    }

    // Corps envoye sans l'en-tete Content-Type: application/json.
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErreurReponse> typeNonSupporte(HttpMediaTypeNotSupportedException ex) {
        return construire(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Format non supporté : envoyer du JSON.", Map.of());
    }

    // ---- 500 : tout le reste ----

    // Filet final : toute exception que personne n'a prevue.
    // La trace complete va dans le journal du serveur ; le client ne recoit
    // qu'un message neutre. Une pile d'appels visible = information offerte aux pirates.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurReponse> imprevue(Exception ex) {
        JOURNAL.error("Erreur interne non prevue", ex);
        return construire(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue.", Map.of());
    }

    // Methode d'aide : fabrique la meme reponse pour tous les cas.
    private ResponseEntity<ErreurReponse> construire(HttpStatus statut, String message,
                                                     Map<String, String> erreurs) {
        ErreurReponse corps = new ErreurReponse(LocalDateTime.now(), statut.value(), message, erreurs);
        return ResponseEntity.status(statut).body(corps);
    }
}