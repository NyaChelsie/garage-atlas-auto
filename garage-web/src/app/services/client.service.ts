import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Page } from '../models/page.model';
import { Client, ClientRequete } from '../models/client.model';

// @Injectable({ providedIn: 'root' }) : Angular cree UN SEUL exemplaire de ce service
// pour toute l'application, et le donne a qui le demande.
@Injectable({ providedIn: 'root' })
export class ClientService {
  // inject(HttpClient) : Angular nous fournit l'outil qui envoie les requetes HTTP.
  // C'est possible grace a provideHttpClient() dans app.config.ts.
  private http = inject(HttpClient);

  // L'adresse de base vient du fichier d'environnement : jamais ecrite ici en dur.
  private url = `${environment.apiUrl}/clients`;

  // GET /api/clients?page=0&taille=10&recherche=ab  -> Page<Client>
  // Un Observable est "paresseux" : la requete ne part que lorsque quelqu'un s'abonne
  // (subscribe, ou le pipe async dans le template).
  lister(page = 0, taille = 10, recherche = ''): Observable<Page<Client>> {
    // HttpParams construit proprement la partie "?page=0&taille=10..." de l'adresse.
    let params = new HttpParams().set('page', page).set('taille', taille);
    // On n'envoie "recherche" que s'il y a du texte : sinon on ne l'ajoute pas du tout.
    if (recherche.trim() !== '') {
      params = params.set('recherche', recherche.trim());
    }
    return this.http.get<Page<Client>>(this.url, { params });
  }

  // GET /api/clients/3  -> un seul client
  lire(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.url}/${id}`);
  }

  // POST /api/clients  -> le serveur repond 201 avec le client cree (id compris)
  creer(client: ClientRequete): Observable<Client> {
    return this.http.post<Client>(this.url, client);
  }

  // PUT /api/clients/3  -> remplace la fiche complete
  modifier(id: number, client: ClientRequete): Observable<Client> {
    return this.http.put<Client>(`${this.url}/${id}`, client);
  }

  // DELETE /api/clients/3  -> le serveur repond 204 (aucun contenu) : d'ou void.
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
