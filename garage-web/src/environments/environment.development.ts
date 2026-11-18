// Fichier utilise quand on lance l'application avec "ng serve" (developpement).
// C'est le SEUL endroit du projet ou l'adresse de l'API est ecrite.
// Les services Angular importent cette valeur : aucun composant ne contient d'adresse.
export const environment = {
  production: false,
  // Adresse du back Spring Boot, avec le prefixe /api impose par le sujet.
  apiUrl: 'http://localhost:8080/api',
};
