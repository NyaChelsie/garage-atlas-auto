package com.example.garage_api.exception;

import java.time.LocalDateTime;
import java.util.Map;

// La "fiche d'incident" : le meme format pour TOUTES les erreurs de l'API.
public record ErreurReponse(
        // Quand l'erreur s'est produite.
        LocalDateTime horodatage,
        // Le code HTTP (400, 404, 409, 500...).
        int code,
        // Un message lisible, en francais, sans aucune information technique.
        String message,
        // Pour une validation : champ -> message. Vide {} dans les autres cas.
        Map<String, String> erreurs
) {
}