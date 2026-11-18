import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

import { Badge } from '../../components/badge/badge';
import { Pagination } from '../../components/pagination/pagination';
import { Client } from '../../models/client.model';
import { messageErreur } from '../../models/erreur.model';
import {
  LIBELLES_STATUT,
  StatutVehicule,
  TRANSITIONS,
  Vehicule,
} from '../../models/vehicule.model';
import { ClientService } from '../../services/client.service';
import { NotificationService } from '../../services/notification.service';
import { VehiculeService } from '../../services/vehicule.service';

// Les choix du filtre de statut : la valeur envoyee au serveur + le texte affiche.
const STATUTS: { valeur: StatutVehicule; libelle: string }[] = [
  { valeur: 'EN_ATTENTE', libelle: 'En attente' },
  { valeur: 'EN_REPARATION', libelle: 'En réparation' },
  { valeur: 'TERMINE', libelle: 'Terminé' },
  { valeur: 'LIVRE', libelle: 'Livré' },
];

@Component({
  selector: 'app-vehicules',
  imports: [RouterLink, Badge, Pagination],
  templateUrl: './vehicules.html',
  styleUrl: './vehicules.css',
})
export class Vehicules {
  private vehiculeService = inject(VehiculeService);
  private clientService = inject(ClientService);
  private notification = inject(NotificationService);

  private readonly TAILLE = 10;

  // Liste des statuts, lue par le HTML pour remplir le filtre.
  protected statuts = STATUTS;

  // ---------- L'etat de l'ecran ----------
  protected vehicules = signal<Vehicule[]>([]);
  protected clients = signal<Client[]>([]);   // pour la liste deroulante "proprietaire"
  protected page = signal(0);
  protected totalPages = signal(0);
  protected totalElements = signal(0);
  protected chargement = signal(true);
  protected erreur = signal<string | null>(null);

  // Id du vehicule dont le statut est en cours de changement (null = aucun).
  // Sert a desactiver sa liste : pas de double envoi.
  protected changementEnCours = signal<number | null>(null);

  // ---------- Les filtres courants ----------
  private recherche = '';
  private statut: StatutVehicule | undefined;   // undefined = pas de filtre
  private clientId: number | undefined;
  private saisie$ = new Subject<string>();

  constructor() {
    // Meme principe que la page des clients : 300 ms de pause avant de chercher.
    this.saisie$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((texte) => {
        this.recherche = texte;
        this.page.set(0);
        this.charger();
      });

    // On charge les clients pour la liste deroulante (100 d'un coup suffisent ici).
    this.clientService.lister(0, 100).subscribe({
      next: (resultat) => this.clients.set(resultat.contenu),
      error: (e: HttpErrorResponse) => this.notification.erreur(messageErreur(e)),
    });

    this.charger();
  }

  protected rechercher(texte: string): void {
    this.saisie$.next(texte);
  }

  // Appelee quand l'utilisateur choisit un statut. '' = "Tous les statuts".
  protected filtrerStatut(valeur: string): void {
    // find : cherche la valeur dans la liste ; on obtient ainsi un vrai StatutVehicule
    // (ou undefined), sans convertir de force le texte du HTML.
    this.statut = STATUTS.find((s) => s.valeur === valeur)?.valeur;
    this.page.set(0);   // un filtre change : retour a la page 1
    this.charger();
  }

  // Appelee quand l'utilisateur choisit un proprietaire. '' = "Tous les clients".
  protected filtrerClient(valeur: string): void {
    this.clientId = valeur === '' ? undefined : Number(valeur);
    this.page.set(0);
    this.charger();
  }

  protected changerPage(numero: number): void {
    this.page.set(numero);
    this.charger();
  }

  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    this.vehiculeService
      .lister(this.page(), this.TAILLE, this.recherche, this.statut, this.clientId)
      .subscribe({
        next: (resultat) => {
          this.vehicules.set(resultat.contenu);
          this.totalPages.set(resultat.totalPages);
          this.totalElements.set(resultat.totalElements);
          this.chargement.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.erreur.set(messageErreur(e));
          this.chargement.set(false);
        },
      });
  }

  // ---------- Changement de statut ----------

  // Les statuts que l'on a le droit de PROPOSER pour ce vehicule :
  // on lit le tableau des transitions, depuis son statut actuel.
  protected statutsProposes(vehicule: Vehicule): { valeur: StatutVehicule; libelle: string }[] {
    // map : transforme chaque statut en { valeur, libelle } pour remplir la liste.
    return TRANSITIONS[vehicule.statut].map((s) => ({ valeur: s, libelle: LIBELLES_STATUT[s] }));
  }

  // Appelee quand l'utilisateur choisit un statut dans la liste d'une ligne.
  protected changerStatut(vehicule: Vehicule, valeur: string): void {
    // On retrouve un vrai StatutVehicule parmi ceux proposes (ou undefined).
    // '' (le texte "Changer le statut...") ne correspond a rien : on ne fait rien.
    const nouveau = this.statutsProposes(vehicule).find((s) => s.valeur === valeur);
    if (!nouveau) {
      return;
    }

    this.changementEnCours.set(vehicule.id);

    this.vehiculeService.changerStatut(vehicule.id, nouveau.valeur).subscribe({
      next: () => {
        this.changementEnCours.set(null);
        this.notification.succes(`${vehicule.immatriculation} : ${nouveau.libelle}.`);
        // On relit la liste : le badge et les choix proposes viennent du serveur.
        this.charger();
      },
      // 409 (liste perimee, transition devenue interdite) : on affiche le message du serveur.
      error: (e: HttpErrorResponse) => {
        this.changementEnCours.set(null);
        this.notification.erreur(messageErreur(e));
        this.charger();
      },
    });
  }

  protected supprimer(vehicule: Vehicule): void {
    if (!confirm(`Supprimer le véhicule ${vehicule.immatriculation} ?`)) {
      return;
    }

    this.vehiculeService.supprimer(vehicule.id).subscribe({
      next: () => {
        this.notification.succes('Véhicule supprimé.');
        // Derniere ligne d'une page autre que la premiere : on recule d'une page.
        if (this.vehicules().length === 1 && this.page() > 0) {
          this.page.update((p) => p - 1);
        }
        this.charger();
      },
      // 409 : le vehicule a des interventions. On affiche le message du serveur.
      error: (e: HttpErrorResponse) => this.notification.erreur(messageErreur(e)),
    });
  }

  // Met un montant en forme : 120000 devient "120 000 FCFA".
  // Intl.NumberFormat : l'outil du navigateur pour formater les nombres selon une langue.
  protected fcfa(montant: number): string {
    return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
  }
}