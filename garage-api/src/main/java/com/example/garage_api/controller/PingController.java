// 📍 PACKAGE = l'adresse du fichier dans le projet.
// Doit être EXACTEMENT celle de ton arbre (avec le tiret bas).
package com.example.garage_api.controller;

// 📥 IMPORTS = on va chercher les outils dont on a besoin.
import java.util.Map; // Map = dictionnaire (clé → valeur), il deviendra du JSON
import org.springframework.web.bind.annotation.GetMapping;     // pour déclarer une route GET
import org.springframework.web.bind.annotation.RequestMapping; // pour donner un préfixe commun aux routes
import org.springframework.web.bind.annotation.RestController; // pour dire "cette classe répond en JSON"

// 🛎️ @RestController = "cette classe est un COMPTOIR".
// Elle reçoit des requêtes HTTP et renvoie des données (JSON), pas des pages HTML.
@RestController
// 🏷️ Toutes les routes de cette classe commencent par /api
@RequestMapping("/api")
public class PingController {

    // 🔗 "Quand quelqu'un fait GET /api/ping, exécute la méthode juste dessous."
    // (/api vient du @RequestMapping, /ping vient d'ici)
    @GetMapping("/ping")
    // Cette méthode renvoie un dictionnaire texte → texte.
    // Spring le transforme tout seul en JSON.
    public Map<String, String> ping() {
        // Map.of("message", "pong") fabrique : { "message": "pong" }
        return Map.of("message", "pong");
    }
}