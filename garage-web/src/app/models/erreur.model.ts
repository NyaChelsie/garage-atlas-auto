import { HttpErrorResponse } from '@angular/common/http';

// Le format d'erreur de l'API : miroir de ErreurReponse cote Spring Boot.
// Toutes les erreurs (400, 404, 409, 500) ont exactement cette forme.
export interface ErreurApi {
  horodatage: string;                // date et heure de l'erreur
  code: number;                      // 400, 404, 409, 500...
  message: string;                   // message lisible, en francais
  erreurs: Record<string, string>;   // "champ" -> "message" ; {} sauf pour une validation (400)
}

// Verifie que le corps d'une erreur HTTP a bien la forme d'une ErreurApi.
// "unknown" : on ne fait confiance a rien tant qu'on n'a pas verifie (pas de any).
// "corps is ErreurApi" : si la fonction renvoie true, TypeScript traite ensuite
// le corps comme une ErreurApi.
function estErreurApi(corps: unknown): corps is ErreurApi {
  return (
    typeof corps === 'object' &&
    corps !== null &&
    'message' in corps &&
    typeof corps.message === 'string'
  );
}

// Transforme une erreur HTTP en UN message lisible a afficher a l'utilisateur.
export function messageErreur(erreur: HttpErrorResponse): string {
  // status 0 : la requete n'a jamais atteint le serveur (back eteint, reseau coupe).
  if (erreur.status === 0) {
    return 'Impossible de joindre le serveur. Vérifiez que le back est lancé.';
  }
  // Cas normal : le serveur a repondu avec son JSON d'erreur uniforme.
  if (estErreurApi(erreur.error)) {
    return erreur.error.message;
  }
  // Reponse inattendue (pas notre format) : message neutre, sans detail technique.
  return 'Une erreur est survenue.';
}

// Extrait la liste "champ -> message" d'une erreur 400 de validation.
// Les formulaires (etape 10) s'en serviront pour afficher chaque message sous son champ.
// Renvoie {} pour toute autre erreur.
export function erreursChamps(erreur: HttpErrorResponse): Record<string, string> {
  if (estErreurApi(erreur.error) && erreur.error.erreurs) {
    return erreur.error.erreurs;
  }
  return {};
}
