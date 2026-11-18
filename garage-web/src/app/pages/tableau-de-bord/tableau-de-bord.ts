import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { messageErreur } from '../../models/erreur.model';
import { Piece } from '../../models/piece.model';
import {
  InterventionStatistiques,
  PieceStatistiques,
  VehiculeStatistiques,
} from '../../models/statistiques.model';
import { Carburant, LIBELLES_STATUT, StatutVehicule } from '../../models/vehicule.model';
import { InterventionService } from '../../services/intervention.service';
import { PieceService } from '../../services/piece.service';
import { VehiculeService } from '../../services/vehicule.service';

// Tout ce que le tableau de bord recoit du serveur, regroupe en un seul objet.
interface Donnees {
  vehicules: VehiculeStatistiques;
  pieces: PieceStatistiques;
  interventions: InterventionStatistiques;
  alertes: Piece[];
}

// Les cles fixes des repartitions : on boucle sur CES listes (et non sur le JSON recu),
// car le serveur peut omettre une cle qui vaut 0 (c'est le "Partial" du modele).
const STATUTS: StatutVehicule[] = ['EN_ATTENTE', 'EN_REPARATION', 'TERMINE', 'LIVRE'];
const CARBURANTS: { valeur: Carburant; libelle: string }[] = [
  { valeur: 'ESSENCE', libelle: 'Essence' },
  { valeur: 'DIESEL', libelle: 'Diesel' },
  { valeur: 'ELECTRIQUE', libelle: 'Électrique' },
  { valeur: 'HYBRIDE', libelle: 'Hybride' },
];

// Les libelles francais des categories de pieces.
const LIBELLES_CATEGORIE: Record<string, string> = {
  MOTEUR: 'Moteur',
  FREINAGE: 'Freinage',
  SUSPENSION: 'Suspension',
  ELECTRICITE: 'Électricité',
  CARROSSERIE: 'Carrosserie',
  ENTRETIEN: 'Entretien',
};

// Une carte de chiffre cle : un titre, une valeur deja formatee, et un drapeau "alerte".
interface Carte {
  titre: string;
  valeur: string;
  alerte?: boolean;
}

// Une barre de repartition : un texte, une valeur affichee, et une largeur en pourcent.
interface Barre {
  libelle: string;
  valeur: string;
  pct: number;
}

@Component({
  imports: [RouterLink],
  selector: 'app-tableau-de-bord',
  styleUrl: './tableau-de-bord.css',
  templateUrl: './tableau-de-bord.html',
})
export class TableauDeBord {
  private vehiculeService = inject(VehiculeService);
  private pieceService = inject(PieceService);
  private interventionService = inject(InterventionService);

  // Formateur de nombres a la francaise : 15000 devient "15 000".
  private nombreFr = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 });

  // ---------- L'etat de l'ecran ----------
  protected donnees = signal<Donnees | null>(null);
  protected chargement = signal(true);
  protected erreur = signal<string | null>(null);

  // ---------- Les cartes : calculees UNE fois quand les donnees arrivent ----------
  // computed : se recalcule tout seul quand "donnees" change. Aucun calcul d'agregat ici :
  // on ne fait que mettre en forme des chiffres DEJA calcules par le serveur.
  protected cartes = computed<Carte[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    return [
      { titre: 'Véhicules', valeur: this.nombre(d.vehicules.total) },
      {
        titre: 'En réparation',
        valeur: this.nombre(d.vehicules.parStatut['EN_REPARATION'] ?? 0),
      },
      { titre: 'Kilométrage moyen', valeur: this.nombre(d.vehicules.kilometrageMoyen) + ' km' },
      {
        titre: 'Coût estimé des réparations en cours',
        valeur: this.fcfa(d.vehicules.coutEstimeEnReparation),
      },
      { titre: 'Références de pièces', valeur: this.nombre(d.pieces.nombreReferences) },
      { titre: 'Valeur du stock', valeur: this.fcfa(d.pieces.valeurStock) },
      {
        titre: 'Pièces en alerte',
        valeur: this.nombre(d.pieces.enAlerte),
        alerte: d.pieces.enAlerte > 0,
      },
    ];
  });

  // Indicateurs issus des relations (clients, interventions, vehicules sans intervention).
  protected cartesRelations = computed<Carte[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    return [
      { titre: 'Clients', valeur: this.nombre(d.interventions.nombreClients) },
      { titre: 'Interventions', valeur: this.nombre(d.interventions.nombreInterventions) },
      { titre: 'Chiffre d\'affaires du mois', valeur: this.fcfa(d.interventions.chiffreAffairesMois) },
      { titre: 'Coût moyen d\'une intervention', valeur: this.fcfa(d.interventions.coutMoyenIntervention) },
      { titre: 'Véhicules entrés ce mois-ci', valeur: this.nombre(d.vehicules.entresCeMois) },
      { titre: 'Véhicules sans intervention', valeur: this.nombre(d.interventions.vehiculesSansIntervention) },
    ];
  });

  // ---------- Les barres de repartition ----------
  // Base de chaque barre = le TOTAL donne par le serveur (jamais un total refait ici).

  protected barresStatut = computed<Barre[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    return STATUTS.map((s) => {
      const n = d.vehicules.parStatut[s] ?? 0;   // cle absente = 0
      return { libelle: LIBELLES_STATUT[s], valeur: this.nombre(n), pct: this.pct(n, d.vehicules.total) };
    });
  });

  protected barresCarburant = computed<Barre[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    return CARBURANTS.map((c) => {
      const n = d.vehicules.parCarburant[c.valeur] ?? 0;
      return { libelle: c.libelle, valeur: this.nombre(n), pct: this.pct(n, d.vehicules.total) };
    });
  });

  protected barresMarques = computed<Barre[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    return d.vehicules.topMarques.map((m) => ({
      libelle: m.marque,
      valeur: this.nombre(m.nombre),
      pct: this.pct(m.nombre, d.vehicules.total),
    }));
  });

  // Pieces par categorie : DEUX barres par categorie (le nombre, puis la valeur).
  protected categories = computed(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    return d.pieces.parCategorie.map((c) => ({
      libelle: LIBELLES_CATEGORIE[c.categorie] ?? c.categorie,
      nombre: this.nombre(c.nombre),
      valeur: this.fcfa(c.valeur),
      pctNombre: this.pct(c.nombre, d.pieces.nombreReferences),
      pctValeur: this.pct(c.valeur, d.pieces.valeurStock),
    }));
  });

  // Top clients et top pieces : le serveur les renvoie deja tries du plus grand au plus petit.
  // La base est donc le PREMIER element (la barre du N°1 remplit toute la largeur).
  protected barresClients = computed<Barre[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    const maximum = d.interventions.topClients[0]?.montant ?? 0;
    return d.interventions.topClients.map((c) => ({
      libelle: c.nom,
      valeur: this.fcfa(c.montant),
      pct: this.pct(c.montant, maximum),
    }));
  });

  protected barresPieces = computed<Barre[]>(() => {
    const d = this.donnees();
    if (!d) {
      return [];
    }
    const maximum = d.interventions.topPieces[0]?.quantite ?? 0;
    return d.interventions.topPieces.map((p) => ({
      libelle: `${p.reference} · ${p.designation}`,
      valeur: this.nombre(p.quantite) + ' utilisée(s)',
      pct: this.pct(p.quantite, maximum),
    }));
  });

  constructor() {
    this.charger();
  }

  // Un seul appel groupe : un seul etat "chargement" et un seul message d'erreur.
  protected charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    // forkJoin : lance les 4 appels en meme temps et attend que les 4 reviennent.
    forkJoin({
      vehicules: this.vehiculeService.statistiques(),
      pieces: this.pieceService.statistiques(),
      interventions: this.interventionService.statistiques(),
      alertes: this.pieceService.alertes(),
    }).subscribe({
      next: (d) => {
        this.donnees.set(d);
        this.chargement.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.chargement.set(false);
      },
    });
  }

  // ---------- Mise en forme ----------

  // Largeur de barre en pourcent. Base 0 (liste vide) = 0 : jamais de division par zero,
  // donc jamais de NaN a l'ecran. Math.min plafonne a 100 pour ne jamais deborder.
  private pct(valeur: number, base: number): number {
    return base > 0 ? Math.min(100, (valeur / base) * 100) : 0;
  }

  // 15000 devient "15 000".
  protected nombre(valeur: number): string {
    return this.nombreFr.format(valeur);
  }

  // 15000 devient "15 000 FCFA".
  protected fcfa(montant: number): string {
    return this.nombreFr.format(montant) + ' FCFA';
  }
}