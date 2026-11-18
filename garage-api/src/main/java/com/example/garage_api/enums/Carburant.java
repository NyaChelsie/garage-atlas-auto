// Le package indique ou se trouve ce fichier dans le projet.
package com.example.garage_api.enums;

// "enum" = une liste fermee de valeurs : on ne peut pas en inventer d'autres.
// Un vehicule ne peut avoir QUE un de ces 4 carburants.
public enum Carburant {
    ESSENCE,     // premiere valeur
    DIESEL,      // deuxieme valeur
    ELECTRIQUE,  // troisieme valeur
    HYBRIDE      // derniere valeur : pas de virgule apres
}