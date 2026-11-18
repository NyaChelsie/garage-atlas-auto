import { Injectable, signal } from '@angular/core';

// Un message a deux formes possibles : succes (vert) ou erreur (rouge).
export type TypeMessage = 'succes' | 'erreur';

// Un message = son type + son texte.
export interface Message {
  type: TypeMessage;
  texte: string;
}

// providedIn: 'root' : un seul exemplaire pour toute l'application.
// N'importe quelle page peut donc afficher un message, et la zone de notification
// (placee dans app.html) le montrera partout.
@Injectable({ providedIn: 'root' })
export class NotificationService {
  // signal : une "boite" dont le contenu met l'ecran a jour quand il change.
  // null = aucun message affiche.
  // "private" + "asReadonly" ci-dessous : les pages peuvent LIRE le message,
  // mais seul ce service peut le MODIFIER.
  private _message = signal<Message | null>(null);
  readonly message = this._message.asReadonly();

  // Identifiant du minuteur, pour pouvoir l'annuler si un nouveau message arrive.
  private minuteur: ReturnType<typeof setTimeout> | undefined;

  // Affiche un message vert (ex : "Vehicule cree avec succes").
  succes(texte: string): void {
    this.afficher({ type: 'succes', texte });
  }

  // Affiche un message rouge (ex : "Cette immatriculation existe deja").
  erreur(texte: string): void {
    this.afficher({ type: 'erreur', texte });
  }

  // Efface le message tout de suite (bouton de fermeture).
  effacer(): void {
    clearTimeout(this.minuteur);
    this._message.set(null);
  }

  // Affiche un message puis l'efface tout seul apres 5 secondes.
  private afficher(message: Message): void {
    // On annule l'ancien minuteur : sinon il effacerait le NOUVEAU message trop tot.
    clearTimeout(this.minuteur);
    this._message.set(message);
    this.minuteur = setTimeout(() => this._message.set(null), 5000);
  }
}
