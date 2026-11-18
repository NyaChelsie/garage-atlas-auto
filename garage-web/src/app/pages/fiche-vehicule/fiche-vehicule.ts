import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { Badge } from '../../components/badge/badge';
import { messageErreur } from '../../models/erreur.model';
import {
  Intervention,
  InterventionRequete,
  TypeIntervention,
} from '../../models/intervention.model';
import { Piece } from '../../models/piece.model';
import { Vehicule } from '../../models/vehicule.model';
import { InterventionService } from '../../services/intervention.service';
import { NotificationService } from '../../services/notification.service';
import { PieceService } from '../../services/piece.service';
import { VehiculeService } from '../../services/vehicule.service';

// Les types d'intervention : la valeur envoyee au serveur + le texte affiche.
const TYPES: { valeur: TypeIntervention; libelle: string }[] = [
  { valeur: 'VIDANGE', libelle: 'Vidange' },
  { valeur: 'FREINS', libelle: 'Freins' },
  { valeur: 'MOTEUR', libelle: 'Moteur' },
  { valeur: 'ELECTRICITE', libelle: 'Électricité' },
  { valeur: 'CARROSSERIE', libelle: 'Carrosserie' },
  { valeur: 'DIAGNOSTIC', libelle: 'Diagnostic' },
];

// Date du jour au format ISO local ("2026-11-18"), le format d'un <input type="date">.
// 'en-CA' donne AAAA-MM-JJ ; comparer deux dates ISO comme du texte donne le bon ordre.
function aujourdhui(): string {
  return new Date().toLocaleDateString('en-CA');
}

// Validateur : une date ne peut pas etre dans le futur (regle du sujet).
function pasDansLeFutur(controle: AbstractControl): ValidationErrors | null {
  const valeur: string = controle.value;
  return valeur && valeur > aujourdhui() ? { futur: true } : null;
}

// Validateur de GROUPE : la date de fin ne peut pas preceder le debut.
// Il regarde deux champs a la fois, c'est pourquoi il est pose sur le groupe entier.
function finApresDebut(groupe: AbstractControl): ValidationErrors | null {
  const debut: string = groupe.get('dateDebut')?.value;
  const fin: string = groupe.get('dateFin')?.value;
  return debut && fin && fin < debut ? { finAvantDebut: true } : null;
}

@Component({
  selector: 'app-fiche-vehicule',
  imports: [RouterLink, ReactiveFormsModule, Badge],
  templateUrl: './fiche-vehicule.html',
  styleUrl: './fiche-vehicule.css',
})
export class FicheVehicule {
  private fb = inject(NonNullableFormBuilder);
  private route = inject(ActivatedRoute);
  private vehiculeService = inject(VehiculeService);
  private interventionService = inject(InterventionService);
  private pieceService = inject(PieceService);
  private notification = inject(NotificationService);

  // L'id vient de l'adresse : /vehicules/3 donne 3.
  private vehiculeId = Number(this.route.snapshot.paramMap.get('id'));

  protected types = TYPES;
  protected aujourdhui = aujourdhui();   // sert d'attribut max aux champs date

  // ---------- L'etat de l'ecran ----------
  protected vehicule = signal<Vehicule | null>(null);
  protected interventions = signal<Intervention[]>([]);
  protected pieces = signal<Piece[]>([]);        // pour la liste deroulante des pieces
  protected chargement = signal(true);
  protected erreur = signal<string | null>(null);

  // ---------- Formulaire "nouvelle intervention" ----------
  protected formulaireOuvert = signal(false);
  protected envoiIntervention = signal(false);
  protected erreurIntervention = signal<string | null>(null);   // message du serveur

  protected formIntervention = this.fb.group(
    {
      type: this.fb.control<TypeIntervention>('DIAGNOSTIC'),
      description: this.fb.control('', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(300),
      ]),
      dateDebut: this.fb.control(aujourdhui(), [Validators.required, pasDansLeFutur]),
      dateFin: this.fb.control(''),                       // facultative : '' = pas de date
      mainOeuvre: this.fb.control<number | null>(null, [Validators.min(0)]),   // facultative
    },
    { validators: [finApresDebut] }
  );

  // ---------- Panneau "ajouter une piece" ----------
  // Id de l'intervention dont le panneau est ouvert (null = aucun). Un seul a la fois.
  protected interventionOuverte = signal<number | null>(null);
  protected envoiLigne = signal(false);
  protected erreurLigne = signal<string | null>(null);

  protected formLigne = this.fb.group({
    pieceId: this.fb.control<number | null>(null, [Validators.required]),
    quantite: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.pattern(/^\d+$/),
      Validators.min(1),
    ]),
  });

  constructor() {
    this.chargerTout();
  }

  // ---------- Chargement ----------

  private chargerTout(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    // forkJoin : lance les deux appels en meme temps et attend que les DEUX reviennent.
    forkJoin({
      vehicule: this.vehiculeService.lire(this.vehiculeId),
      interventions: this.interventionService.duVehicule(this.vehiculeId),
    }).subscribe({
      next: (r) => {
        this.vehicule.set(r.vehicule);
        this.interventions.set(r.interventions);
        this.chargement.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.chargement.set(false);
      },
    });

    this.chargerPieces();
  }

  // Les pieces et leur stock : 100 d'un coup suffisent pour la liste deroulante.
  private chargerPieces(): void {
    this.pieceService.lister(0, 100, '', undefined).subscribe({
      next: (r) => this.pieces.set(r.contenu),
      error: (e: HttpErrorResponse) => this.notification.erreur(messageErreur(e)),
    });
  }

  // Apres une modification : on relit l'historique ET les pieces.
  // Les totaux et les stocks viennent toujours du serveur, le front ne calcule rien.
  private rafraichir(): void {
    this.interventionService.duVehicule(this.vehiculeId).subscribe({
      next: (liste) => this.interventions.set(liste),
      error: (e: HttpErrorResponse) => this.notification.erreur(messageErreur(e)),
    });
    this.chargerPieces();
  }

  // ---------- Nouvelle intervention ----------

  protected basculerFormulaire(): void {
    this.formulaireOuvert.update((ouvert) => !ouvert);
    this.erreurIntervention.set(null);
  }

  // Message sous un champ (ou null s'il n'y a rien a dire).
  protected erreurChamp(nom: 'description' | 'dateDebut' | 'mainOeuvre'): string | null {
    const controle = this.formIntervention.controls[nom];
    if (!controle.touched || !controle.errors) {
      return null;
    }
    if (controle.errors['required']) {
      return 'Ce champ est obligatoire';
    }
    if (controle.errors['minlength'] || controle.errors['maxlength']) {
      return 'La description doit contenir entre 5 et 300 caractères';
    }
    if (controle.errors['futur']) {
      return 'La date ne peut pas être dans le futur';
    }
    if (controle.errors['min']) {
      return 'La main-d\'œuvre ne peut pas être négative';
    }
    return null;
  }

  // Erreur de la date de fin : elle vient du GROUPE (comparaison avec le debut).
  protected erreurFin(): string | null {
    const touche = this.formIntervention.controls.dateFin.touched;
    return touche && this.formIntervention.errors?.['finAvantDebut']
      ? 'La date de fin ne peut pas précéder le début'
      : null;
  }

  protected creerIntervention(): void {
    if (this.formIntervention.invalid) {
      this.formIntervention.markAllAsTouched();   // fait apparaitre tous les messages
      return;
    }

    const v = this.formIntervention.getRawValue();
    // Le corps envoye : miroir de InterventionRequete cote Spring Boot.
    const requete: InterventionRequete = {
      vehiculeId: this.vehiculeId,
      type: v.type,
      description: v.description.trim(),
      dateDebut: v.dateDebut,
      dateFin: v.dateFin === '' ? null : v.dateFin,          // '' devient null
      mainOeuvre: v.mainOeuvre === null ? null : Number(v.mainOeuvre),
    };

    this.envoiIntervention.set(true);
    this.erreurIntervention.set(null);

    this.interventionService.creer(requete).subscribe({
      next: () => {
        this.envoiIntervention.set(false);
        this.notification.succes('Intervention enregistrée.');
        this.formIntervention.reset();       // retour aux valeurs de depart
        this.formulaireOuvert.set(false);
        this.rafraichir();
      },
      // 409 (vehicule livre...) ou 400 : on affiche le message du serveur.
      error: (e: HttpErrorResponse) => {
        this.envoiIntervention.set(false);
        this.erreurIntervention.set(messageErreur(e));
      },
    });
  }

  // ---------- Pieces d'une intervention ----------

  // Les pieces proposables : on retire celles deja presentes sur CETTE intervention
  // (le serveur refuse une meme piece deux fois : couple intervention + piece unique).
  protected piecesDisponibles(intervention: Intervention): Piece[] {
    const dejaUtilisees = new Set(intervention.lignes.map((l) => l.pieceId));
    return this.pieces().filter((p) => !dejaUtilisees.has(p.id));
  }

  // Ouvre ou referme le panneau d'une intervention.
  protected basculerLigne(intervention: Intervention): void {
    if (this.interventionOuverte() === intervention.id) {
      this.interventionOuverte.set(null);
      return;
    }
    this.formLigne.reset();
    this.erreurLigne.set(null);
    this.interventionOuverte.set(intervention.id);
  }

  protected erreurQuantite(): string | null {
    const controle = this.formLigne.controls.quantite;
    if (!controle.touched || !controle.errors) {
      return null;
    }
    return controle.errors['required']
      ? 'La quantité est obligatoire'
      : 'La quantité doit être un entier d\'au moins 1';
  }

  protected ajouterLigne(intervention: Intervention): void {
    if (this.formLigne.invalid) {
      this.formLigne.markAllAsTouched();
      return;
    }

    const { pieceId, quantite } = this.formLigne.getRawValue();
    if (pieceId === null || quantite === null) {   // garde-fou pour TypeScript
      return;
    }

    this.envoiLigne.set(true);
    this.erreurLigne.set(null);

    // Pas de prix envoye : le serveur copie le prix actuel de la piece.
    this.interventionService
      .ajouterLigne(intervention.id, { pieceId: Number(pieceId), quantite: Number(quantite) })
      .subscribe({
        next: () => {
          this.envoiLigne.set(false);
          this.notification.succes('Pièce ajoutée, stock mis à jour.');
          this.interventionOuverte.set(null);
          this.rafraichir();
        },
        // 409 "stock insuffisant" : rien n'est enregistre, le panneau reste ouvert.
        error: (e: HttpErrorResponse) => {
          this.envoiLigne.set(false);
          this.erreurLigne.set(messageErreur(e));
        },
      });
  }

  protected retirerLigne(intervention: Intervention, ligneId: number): void {
    if (!confirm('Retirer cette pièce de l\'intervention ? Le stock sera restitué.')) {
      return;
    }

    this.interventionService.retirerLigne(intervention.id, ligneId).subscribe({
      next: () => {
        this.notification.succes('Pièce retirée, stock restitué.');
        this.rafraichir();
      },
      error: (e: HttpErrorResponse) => this.notification.erreur(messageErreur(e)),
    });
  }

  // ---------- Affichage ----------

  protected libelleType(valeur: TypeIntervention): string {
    return TYPES.find((t) => t.valeur === valeur)?.libelle ?? valeur;
  }

  // "2026-11-18" devient "18/11/2026".
  protected date(iso: string): string {
    return iso.split('-').reverse().join('/');
  }

  // 15000 devient "15 000 FCFA".
  protected fcfa(montant: number): string {
    return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
  }
}