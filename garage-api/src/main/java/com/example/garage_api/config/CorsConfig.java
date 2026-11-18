package com.example.garage_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration : classe de reglages, lue par Spring au demarrage.
// WebMvcConfigurer : l'interface qui permet de personnaliser la couche web.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // L'unique origine autorisee : le front Angular.
    // Jamais "*" : cela autoriserait n'importe quel site a appeler l'API.
    private static final String ORIGINE_FRONT = "http://localhost:4200";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // "/api/**" : toutes les adresses qui commencent par /api.
        registry.addMapping("/api/**")
                // Seul le front a le droit d'appeler l'API depuis un navigateur.
                .allowedOrigins(ORIGINE_FRONT)
                // PATCH doit etre liste explicitement : il sert au statut et au stock.
                // OPTIONS : le navigateur l'envoie avant POST, PUT, PATCH et DELETE
                // pour demander "ai-je le droit ?" (c'est le "preflight").
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // Content-Type : necessaire pour envoyer du JSON.
                .allowedHeaders("Content-Type");
    }
}