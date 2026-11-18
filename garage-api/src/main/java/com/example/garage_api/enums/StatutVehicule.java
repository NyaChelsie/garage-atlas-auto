package com.example.garage_api.enums;

// Les 4 etapes de la vie d'un vehicule dans le garage.
// Ordre voulu : arrive, en reparation, termine, livre.
public enum StatutVehicule {
    EN_ATTENTE,     // le vehicule vient d'arriver
    EN_REPARATION,  // les mecaniciens travaillent dessus
    TERMINE,        // la reparation est finie
    LIVRE           // le client l'a recupere
}