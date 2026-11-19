package com.example.garage_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration : classe de reglages, lue par Spring au demarrage.
// WebMvcConfigurer : l'interface qui permet de personnaliser la couche web.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Les origines autorisees : les fronts qui ont le droit d'appeler l'API.
    // Elles viennent de la variable d'environnement APP_CORS_ORIGINS (plusieurs adresses
    // separees par des virgules). Sans variable, seul le front local est autorise.
    // Jamais "*" : cela autoriserait n'importe quel site a appeler l'API.
    private final String[] origines;

    // @Value : lit la propriete app.cors.origins ; ce qui suit les deux-points
    // est la valeur par defaut. Spring decoupe tout seul le texte aux virgules.
    public CorsConfig(@Value("${app.cors.origins:http://localhost:4200}") String[] origines) {
        this.origines = origines;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // "/api/**" : toutes les adresses qui commencent par /api.
        registry.addMapping("/api/**")
                // Seuls les fronts de la liste ont le droit d'appeler l'API depuis un navigateur.
                .allowedOrigins(origines)
                // PATCH doit etre liste explicitement : il sert au statut et au stock.
                // OPTIONS : le navigateur l'envoie avant POST, PUT, PATCH et DELETE
                // pour demander "ai-je le droit ?" (c'est le "preflight").
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // Content-Type : necessaire pour envoyer du JSON.
                .allowedHeaders("Content-Type");
    }
}
