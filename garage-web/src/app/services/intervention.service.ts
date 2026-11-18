import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Intervention, InterventionRequete, LigneRequete } from '../models/intervention.model';
import { InterventionStatistiques } from '../models/statistiques.model';

// providedIn: 'root' : un seul exemplaire du service pour toute l'application.
@Injectable({ providedIn: 'root' })
export class InterventionService {
  // Outil d'envoi des requetes HTTP, fourni par Angular.
  private http = inject(HttpClient);

  // Adresse de base : elle vient du fichier d'environnement, jamais ecrite ici en dur.
  private url = `${environment.apiUrl}/interventions`;

  // GET /api/vehicules/2/interventions  -> historique d'un vehicule (liste simple, sans page).
  // Attention : l'adresse commence par /vehicules, pas par /interventions.
  duVehicule(vehiculeId: number): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(`${environment.apiUrl}/vehicules/${vehiculeId}/interventions`);
  }

  // GET /api/interventions/3  -> une intervention avec ses lignes et son total
  lire(id: number): Observable<Intervention> {
    return this.http.get<Intervention>(`${this.url}/${id}`);
  }

  // POST /api/interventions  -> 201 avec l'intervention creee.
  // Le serveur refuse (409) si le vehicule est deja LIVRE.
  creer(intervention: InterventionRequete): Observable<Intervention> {
    return this.http.post<Intervention>(this.url, intervention);
  }

  // PUT /api/interventions/3  -> remplace la fiche.
  // vehiculeId doit etre envoye, mais le serveur l'ignore : l'intervention reste sur son vehicule.
  modifier(id: number, intervention: InterventionRequete): Observable<Intervention> {
    return this.http.put<Intervention>(`${this.url}/${id}`, intervention);
  }

  // DELETE /api/interventions/3  -> 204. Le serveur restitue le stock des pieces utilisees.
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  // POST /api/interventions/3/lignes  avec { "pieceId": 3, "quantite": 2 }
  // Le serveur diminue le stock et copie le prix de la piece.
  // Il repond 201 avec l'INTERVENTION complete (lignes et total mis a jour).
  // 409 si le stock est insuffisant : rien n'est enregistre.
  ajouterLigne(id: number, ligne: LigneRequete): Observable<Intervention> {
    return this.http.post<Intervention>(`${this.url}/${id}/lignes`, ligne);
  }

  // DELETE /api/interventions/3/lignes/7  -> 200 avec l'intervention mise a jour (CORRIGE).
  // Le serveur restitue le stock de la piece et recalcule le total.
  retirerLigne(id: number, ligneId: number): Observable<Intervention> {
    return this.http.delete<Intervention>(`${this.url}/${id}/lignes/${ligneId}`);
  }

  // GET /api/interventions/statistiques  -> les chiffres du tableau de bord.
  statistiques(): Observable<InterventionStatistiques> {
    return this.http.get<InterventionStatistiques>(`${this.url}/statistiques`);
  }
}