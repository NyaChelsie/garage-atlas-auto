package com.example.garage_api;

// SpringApplication : l'outil qui demarre tout le back.
import org.springframework.boot.SpringApplication;
// SpringBootApplication : l'etiquette qui dit "ceci est le point de depart".
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication : Spring explore ce paquet et tous ses sous-paquets
// (controller, entity, repository...) pour y trouver ce qu'il doit utiliser.
@SpringBootApplication
public class GarageApiApplication {

    // main : la porte d'entree de tout programme Java.
    public static void main(String[] args) {
        // Demarre l'application (le serveur sur le port 8080, la base H2, etc.).
        SpringApplication.run(GarageApiApplication.class, args);
    }
}