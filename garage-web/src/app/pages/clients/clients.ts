import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
// takeUntilDestroyed : arrete automatiquement l'abonnement quand la page est quittee.
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
// Subject : un "canal" ou l'on pousse des valeurs, que l'on peut ensuite temporiser.
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

import { Pagination } from '../../components/pagination/pagination';
import { Client } from '../../models/client.model';
import { messageErreur } from '../../models/erreur.model';
import { ClientService } from '../../services/client.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-clients',
  // DatePipe : formate les dates dans le HTML. Pagination : notre composant de pages.
  imports: [RouterLink, DatePipe, Pagination],
  templateUrl: './clients.html',
  styleUrl: './clients.css',
})
export class Clients {
  private clientService = inject(ClientService);
  private notification = inject(NotificationService);

  // Nombre de lignes par page.
  private readonly TAILLE = 10;

  // ---------- L'etat de l'ecran : des "boites" (signals) ----------
  protected clients = signal<Client[]>([]);
  protected page = signal(0);                 // la premiere page vaut 0, comme dans l'API
  protected totalPages = signal(0);
  protected totalElements = signal(0);
  protected chargement = signal(true);        // etat 1 : "Chargement..."
  protected erreur = signal<string | null>(null); // etat 3 : message d'erreur du serveur

  // Le texte de recherche valide (apres la pause de 300 ms).
  private recherche = '';
  // Le canal des saisies : chaque frappe y est poussee.
  private saisie$ = new Subject<string>();

  constructor() {
    // debounceTime(300) : on attend 300 ms SANS nouvelle frappe avant d'agir.
    // Taper "toyota" lettre par lettre n'envoie donc qu'UNE requete, pas six.
    // distinctUntilChanged : si le texte est identique au precedent, on ne recharge pas.
    this.saisie$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((texte) => {
        this.recherche = texte;
        // Nouvelle recherche : on revient a la page 1, sinon on pourrait
        // se retrouver sur une page qui n'existe plus.
        this.page.set(0);
        this.charger();
      });

    // Premier chargement a l'ouverture de la page.
    this.charger();
  }

  // Appelee a chaque frappe dans le champ de recherche.
  protected rechercher(texte: string): void {
    this.saisie$.next(texte);
  }

  // Appelee par la pagination quand l'utilisateur change de page.
  protected changerPage(numero: number): void {
    this.page.set(numero);
    this.charger();
  }

  // Demande la page courante au serveur.
  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    // subscribe : sans lui, la requete ne partirait jamais (Observable "paresseux").
    this.clientService.lister(this.page(), this.TAILLE, this.recherche).subscribe({
      next: (resultat) => {
        this.clients.set(resultat.contenu);
        this.totalPages.set(resultat.totalPages);
        this.totalElements.set(resultat.totalElements);
        this.chargement.set(false);
      },
      // HttpErrorResponse : l'objet d'erreur d'Angular ; messageErreur en tire une phrase.
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.chargement.set(false);
      },
    });
  }

  // Supprime un client apres confirmation, puis recharge la liste.
  protected supprimer(client: Client): void {
    // confirm : boite du navigateur ; "Annuler" arrete tout.
    if (!confirm(`Supprimer le client ${client.prenom} ${client.nom} ?`)) {
      return;
    }

    this.clientService.supprimer(client.id).subscribe({
      next: () => {
        this.notification.succes('Client supprimé.');
        // On vient de supprimer le dernier element d'une page qui n'est pas la premiere :
        // on recule d'une page, sinon on afficherait une page vide.
        if (this.clients().length === 1 && this.page() > 0) {
          this.page.update((p) => p - 1);
        }
        this.charger();
      },
      // 409 : le serveur refuse (le client a des vehicules). On affiche SON message.
      error: (e: HttpErrorResponse) => this.notification.erreur(messageErreur(e)),
    });
  }
}
