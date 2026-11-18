// inject = outil pour demander un objet a Angular.
// Injectable = etiquette qui dit "cette classe peut etre pretee a un composant".
import { inject, Injectable } from '@angular/core';

// HttpClient = le "telephone" qui appelle le back.
import { HttpClient } from '@angular/common/http';

// Observable = une reponse "a venir" (la requete n'est pas encore revenue).
import { Observable } from 'rxjs';

// Cette interface decrit la forme du JSON recu : { "message": "pong" }.
// Elle remplace "any", qui est interdit dans le sujet.
export interface PingReponse {
  message: string;
}

// providedIn: 'root' = une seule instance, partagee dans toute l'application.
@Injectable({ providedIn: 'root' })
export class Ping {
  // On demande a Angular de nous donner le HttpClient.
  // C'est possible grace a provideHttpClient() ajoute dans app.config.ts.
  private http = inject(HttpClient);

  // Methode qui prepare l'appel GET vers le back.
  // Elle renvoie un Observable qui contiendra un objet PingReponse.
  getPing(): Observable<PingReponse> {
    // get<PingReponse> = "fais un GET et considere la reponse comme un PingReponse".
    // L'adresse est ecrite ici pour l'instant ; a l'etape 8 on la mettra
    // a un seul endroit, comme le demande le sujet.
    return this.http.get<PingReponse>('http://localhost:8080/api/ping');
  }
}
