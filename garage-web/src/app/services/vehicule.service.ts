import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Page } from '../models/page.model';
import { VehiculeStatistiques } from '../models/statistiques.model';
import { StatutVehicule, Vehicule, VehiculeRequete } from '../models/vehicule.model';

// providedIn: 'root' : un seul exemplaire du service pour toute l'application.
@Injectable({ providedIn: 'root' })
export class VehiculeService {
  // Outil d'envoi des requetes HTTP, fourni par Angular.
  private http = inject(HttpClient);

  // Adresse de base : elle vient du fichier d'environnement, jamais ecrite ici en dur.
  private url = `${environment.apiUrl}/vehicules`;

  // GET /api/vehicules?page=0&taille=10&recherche=toy&statut=EN_REPARATION&clientId=1
  // Les trois filtres sont facultatifs : un parametre n'est ajoute que s'il a une valeur.
  lister(
    page = 0,
    taille = 10,
    recherche = '',
    statut?: StatutVehicule,
    clientId?: number
  ): Observable<Page<Vehicule>> {
    let params = new HttpParams().set('page', page).set('taille', taille);
    if (recherche.trim() !== '') {
      params = params.set('recherche', recherche.trim());
    }
    if (statut) {
      params = params.set('statut', statut);
    }
    // "!== undefined" : distingue "pas de filtre" d'un vrai identifiant.
    if (clientId !== undefined) {
      params = params.set('clientId', clientId);
    }
    return this.http.get<Page<Vehicule>>(this.url, { params });
  }

  // GET /api/vehicules/statistiques  -> les chiffres du tableau de bord (NOUVEAU).
  // Tout est calcule par le serveur : Angular affiche, il ne calcule rien.
  statistiques(): Observable<VehiculeStatistiques> {
    return this.http.get<VehiculeStatistiques>(`${this.url}/statistiques`);
  }

  // GET /api/vehicules/3  -> un seul vehicule
  lire(id: number): Observable<Vehicule> {
    return this.http.get<Vehicule>(`${this.url}/${id}`);
  }

  // POST /api/vehicules  -> 201 avec le vehicule cree
  creer(vehicule: VehiculeRequete): Observable<Vehicule> {
    return this.http.post<Vehicule>(this.url, vehicule);
  }

  // PUT /api/vehicules/3  -> remplace la fiche complete
  modifier(id: number, vehicule: VehiculeRequete): Observable<Vehicule> {
    return this.http.put<Vehicule>(`${this.url}/${id}`, vehicule);
  }

  // PATCH /api/vehicules/3/statut  avec le corps { "statut": "TERMINE" }
  // PATCH : on modifie UNE seule information, le statut, pas toute la fiche.
  // Le serveur repond 409 si la transition n'est pas permise (ex : EN_ATTENTE -> TERMINE).
  changerStatut(id: number, statut: StatutVehicule): Observable<Vehicule> {
    // { statut } est l'ecriture courte de { statut: statut }.
    return this.http.patch<Vehicule>(`${this.url}/${id}/statut`, { statut });
  }

  // DELETE /api/vehicules/3  -> 204, aucun contenu : d'ou void.
  // Le serveur repond 409 si le vehicule a des interventions.
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  // GET /api/clients/1/vehicules  -> liste simple (pas de page) des vehicules d'un client.
  // Attention : l'adresse commence par /clients, pas par /vehicules.
  duClient(clientId: number): Observable<Vehicule[]> {
    return this.http.get<Vehicule[]>(`${environment.apiUrl}/clients/${clientId}/vehicules`);
  }
}