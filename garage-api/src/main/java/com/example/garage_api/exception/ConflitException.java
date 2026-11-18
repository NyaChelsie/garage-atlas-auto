package com.example.garage_api.exception;

// Utilisee pour tout ce qui entre en CONFLIT avec une regle metier :
//   - un doublon (immatriculation, reference, telephone deja pris)
//   - une transition de statut interdite
//   - une suppression protegee (client qui a des vehicules...)
//   - un stock insuffisant
// Le gestionnaire global (etape 5) la transformera en reponse 409.
public class ConflitException extends RuntimeException {

    public ConflitException(String message) {
        super(message);
    }
}