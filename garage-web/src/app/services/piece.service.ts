import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Page } from '../models/page.model';
import { CategoriePiece, OperationStock, Piece, PieceRequete, StockRequete } from '../models/piece.model';
import { PieceStatistiques } from '../models/statistiques.model';

// providedIn: 'root' : un seul exemplaire du service pour toute l'application.
@Injectable({ providedIn: 'root' })
export class PieceService {
  // Outil d'envoi des requetes HTTP, fourni par Angular.
  private http = inject(HttpClient);

  // Adresse de base : elle vient du fichier d'environnement, jamais ecrite ici en dur.
  private url = `${environment.apiUrl}/pieces`;

  // GET /api/pieces?page=0&taille=10&recherche=frein&categorie=FREINAGE
  // recherche et categorie sont facultatives : un parametre n'est ajoute que s'il a une valeur.
  lister(
    page = 0,
    taille = 10,
    recherche = '',
    categorie?: CategoriePiece
  ): Observable<Page<Piece>> {
    let params = new HttpParams().set('page', page).set('taille', taille);
    if (recherche.trim() !== '') {
      params = params.set('recherche', recherche.trim());
    }
    if (categorie) {
      params = params.set('categorie', categorie);
    }
    return this.http.get<Page<Piece>>(this.url, { params });
  }

  // GET /api/pieces/alertes  -> liste simple des pieces dont quantite <= seuil
  alertes(): Observable<Piece[]> {
    return this.http.get<Piece[]>(`${this.url}/alertes`);
  }

  // GET /api/pieces/statistiques  -> les chiffres du tableau de bord.
  // Tout est calcule par le serveur : Angular affiche, il ne calcule rien.
  statistiques(): Observable<PieceStatistiques> {
    return this.http.get<PieceStatistiques>(`${this.url}/statistiques`);
  }

  // GET /api/pieces/3  -> une seule piece
  lire(id: number): Observable<Piece> {
    return this.http.get<Piece>(`${this.url}/${id}`);
  }

  // POST /api/pieces  -> 201 avec la piece creee
  creer(piece: PieceRequete): Observable<Piece> {
    return this.http.post<Piece>(this.url, piece);
  }

  // PUT /api/pieces/3  -> remplace la fiche.
  // Le serveur ignore quantiteStock ici : le stock ne bouge que par mouvementStock.
  modifier(id: number, piece: PieceRequete): Observable<Piece> {
    return this.http.put<Piece>(`${this.url}/${id}`, piece);
  }

  // PATCH /api/pieces/3/stock  avec { "operation": "SORTIE", "quantite": 2 }
  // Le serveur repond 409 si la sortie depasse le stock disponible.
  mouvementStock(id: number, operation: OperationStock, quantite: number): Observable<Piece> {
    const corps: StockRequete = { operation, quantite };
    return this.http.patch<Piece>(`${this.url}/${id}/stock`, corps);
  }

  // DELETE /api/pieces/3  -> 204, aucun contenu : d'ou void.
  // Le serveur repond 409 si la piece est utilisee dans une intervention.
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
