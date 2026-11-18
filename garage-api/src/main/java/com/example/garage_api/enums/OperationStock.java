package com.example.garage_api.enums;

// Les deux seules operations possibles sur le stock d'une piece.
// ENTREE : on ajoute des pieces (reapprovisionnement).
// SORTIE : on retire des pieces (vente, utilisation).
//
// Pourquoi un enum et pas un simple texte ?
// Avec un texte, une faute de frappe comme "ENTRE" serait acceptee et traitee
// comme une autre operation. Avec un enum, toute valeur inconnue est refusee
// automatiquement par Spring (erreur 400) : rien n'est devine.
public enum OperationStock {
    ENTREE,
    SORTIE
}