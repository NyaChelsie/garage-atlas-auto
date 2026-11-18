// Ligne 1 : on importe deux outils d'Angular (deja la, ne pas toucher).
// ApplicationConfig = le "type" de notre liste d'equipements.
// provideBrowserGlobalErrorListeners = attrape les erreurs non gerees du navigateur.
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';

// Ligne 2 : on importe l'outil de navigation entre pages (deja la).
import { provideRouter } from '@angular/router';

// Ligne 3 : on importe nos routes, definies dans app.routes.ts (deja la).
import { routes } from './app.routes';

// NOUVEAU : on importe l'outil qui active les appels HTTP vers le back.
// '@angular/common/http' est le paquet d'Angular qui gere ces appels.
import { provideHttpClient } from '@angular/common/http';

// On exporte la configuration pour que main.ts puisse la lire.
export const appConfig: ApplicationConfig = {
  // providers = la liste des equipements fournis a toute l'application.
  providers: [
    provideBrowserGlobalErrorListeners(), // filet de securite pour les erreurs (deja la)
    provideRouter(routes),                // active la navigation (deja la)
    // NOUVEAU : on active HttpClient pour toute l'application.
    // Sans cette ligne, Angular plante avec "No provider for HttpClient".
    provideHttpClient(),
  ]
};
