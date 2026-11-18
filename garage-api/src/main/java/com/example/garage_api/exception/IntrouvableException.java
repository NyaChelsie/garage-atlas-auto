package com.example.garage_api.exception;

// RuntimeException : une exception "non controlee". On n'est PAS oblige de la declarer
// avec "throws" a chaque methode. Elle remonte seule jusqu'au gestionnaire global
// (etape 5), qui la transformera en reponse 404.
public class IntrouvableException extends RuntimeException {

    // Le constructeur recoit un message lisible, par exemple "Client introuvable : 99".
    public IntrouvableException(String message) {
        // super(...) : on transmet le message a la classe mere RuntimeException,
        // qui le garde et permet de le relire avec getMessage().
        super(message);
    }
}