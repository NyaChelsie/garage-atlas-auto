import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { erreursChamps, messageErreur } from '../../models/erreur.model';
import { CategoriePiece, PieceRequete } from '../../models/piece.model';
import { NotificationService } from '../../services/notification.service';
import { PieceService } from '../../services/piece.service';
import { aujourdhui, pasDansLeFutur } from '../../validators/dates.validator';

// Les noms des champs : TypeScript refuse toute faute de frappe.
type ChampPiece =
  | 'reference'
  | 'designation'
  | 'categorie'
  | 'fabricant'
  | 'prixUnitaire'
  | 'quantiteStock'
  | 'seuilAlerte'
  | 'dateAchat';

// Les choix de la liste deroulante : valeur envoyee au serveur + texte affiche.
// (Meme liste que dans pieces.ts ; on pourra la mettre en commun plus tard.)
const CATEGORIES: { valeur: CategoriePiece; libelle: string }[] = [
  { valeur: 'MOTEUR', libelle: 'Moteur' },
  { valeur: 'FREINAGE', libelle: 'Freinage' },
  { valeur: 'SUSPENSION', libelle: 'Suspension' },
  { valeur: 'ELECTRICITE', libelle: 'Électricité' },
  { valeur: 'CARROSSERIE', libelle: 'Carrosserie' },
  { valeur: 'ENTRETIEN', libelle: 'Entretien' },
];

// Validateur : le prix doit etre STRICTEMENT superieur a 0 (0 est refuse).
// Validators.min(0) accepterait 0 : il faut donc notre propre regle.
function strictementPositif(control: AbstractControl): ValidationErrors | null {
  const valeur = control.value;
  // Champ vide : la regle "obligatoire" s'en occupe, pas celle-ci.
  if (valeur === null || valeur === '') {
    return null;
  }
  return Number(valeur) > 0 ? null : { positif: true };
}

@Component({
  selector: 'app-piece-formulaire',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './piece-formulaire.html',
  styleUrl: './piece-formulaire.css',
})
export class PieceFormulaire {
  private fb = inject(NonNullableFormBuilder);
  private pieceService = inject(PieceService);
  private notification = inject(NotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Mode : /pieces/3/modifier contient un id, /pieces/nouveau n'en contient pas.
  protected id: number | null = this.lireId();
  protected modification = this.id !== null;

  protected envoiEnCours = signal(false);
  protected chargement = signal(this.modification);
  protected bandeau = signal<string | null>(null);

  // Lus par le HTML : la limite du champ date et la liste des categories.
  protected aujourdhui = aujourdhui();
  protected categories = CATEGORIES;

  // Le formulaire : memes regles que PieceRequete.java cote serveur.
  protected formulaire = this.fb.group({
    // Format PCE-0001 : les lettres PCE, un tiret, puis 4 chiffres.
    reference: ['', [Validators.required, Validators.pattern(/^PCE-\d{4}$/)]],
    designation: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
    // '' = "aucune categorie choisie" ; required refuse ce cas.
    categorie: this.fb.control<CategoriePiece | ''>('', Validators.required),
    fabricant: ['', [Validators.required]],
    // Un nombre peut etre vide : on le type "number | null" (null = champ vide).
    prixUnitaire: this.fb.control<number | null>(null, [Validators.required, strictementPositif]),
    // Entier positif ou nul : uniquement des chiffres.
    quantiteStock: this.fb.control<number | null>(0, [Validators.required, Validators.pattern(/^\d+$/)]),
    seuilAlerte: this.fb.control<number | null>(null, [Validators.required, Validators.pattern(/^\d+$/)]),
    dateAchat: ['', [Validators.required, pasDansLeFutur]],
  });

  constructor() {
    if (this.id !== null) {
      // Le stock ne se change pas par ce formulaire : le serveur l'ignore en modification.
      // disable() : le champ est affiche mais verrouille.
      this.formulaire.controls.quantiteStock.disable();

      this.pieceService.lire(this.id).subscribe({
        next: (piece) => {
          this.formulaire.patchValue({
            reference: piece.reference,
            designation: piece.designation,
            categorie: piece.categorie,
            fabricant: piece.fabricant,
            prixUnitaire: piece.prixUnitaire,
            quantiteStock: piece.quantiteStock,
            seuilAlerte: piece.seuilAlerte,
            dateAchat: piece.dateAchat,
          });
          this.chargement.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.bandeau.set(messageErreur(e));
          this.chargement.set(false);
        },
      });
    }
  }

  // Lit l'id dans l'adresse : "3" devient 3, absent devient null.
  private lireId(): number | null {
    const texte = this.route.snapshot.paramMap.get('id');
    return texte === null ? null : Number(texte);
  }

  // Le message a afficher sous un champ (ou null s'il n'y a rien a dire).
  protected erreur(champ: ChampPiece): string | null {
    const controle = this.formulaire.controls[champ];
    if (!controle.touched || !controle.errors) {
      return null;
    }
    const e = controle.errors;
    // L'erreur renvoyee par le serveur passe en premier.
    if (e['serveur']) return String(e['serveur']);
    if (e['required']) return 'Ce champ est obligatoire';
    if (e['minlength']) return `Minimum ${e['minlength'].requiredLength} caractères`;
    if (e['maxlength']) return `Maximum ${e['maxlength'].requiredLength} caractères`;
    if (e['pattern']) {
      // Le meme mot "pattern" sert a trois champs : le message depend du champ.
      return champ === 'reference'
        ? 'Format attendu : PCE-0001'
        : 'Entier positif ou nul attendu';
    }
    if (e['positif']) return 'Le prix doit être strictement supérieur à 0';
    if (e['futur']) return 'La date ne peut pas être dans le futur';
    return 'Valeur invalide';
  }

  protected enregistrer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    const valeurs = this.formulaire.getRawValue();
    // Garde-fou pour TypeScript : un formulaire valide ne passe jamais ici avec ces valeurs.
    // Il rassure aussi le compilateur : apres ce test, ces champs ne sont ni '' ni null.
    if (
      valeurs.categorie === '' ||
      valeurs.prixUnitaire === null ||
      valeurs.quantiteStock === null ||
      valeurs.seuilAlerte === null
    ) {
      return;
    }

    this.envoiEnCours.set(true);
    this.bandeau.set(null);

    const requete: PieceRequete = {
      reference: valeurs.reference.trim(),
      designation: valeurs.designation.trim(),
      categorie: valeurs.categorie,
      fabricant: valeurs.fabricant.trim(),
      prixUnitaire: Number(valeurs.prixUnitaire),
      quantiteStock: Number(valeurs.quantiteStock),
      seuilAlerte: Number(valeurs.seuilAlerte),
      dateAchat: valeurs.dateAchat,
    };

    const appel =
      this.id === null
        ? this.pieceService.creer(requete)
        : this.pieceService.modifier(this.id, requete);

    appel.subscribe({
      next: () => {
        this.notification.succes(this.id === null ? 'Pièce créée.' : 'Pièce modifiée.');
        // Navigation relative vers la liste : meme principe que pour les clients.
        this.router.navigate([this.id === null ? '..' : '../..'], { relativeTo: this.route });
      },
      error: (e: HttpErrorResponse) => {
        this.envoiEnCours.set(false);
        this.afficherErreursServeur(e);
      },
    });
  }

  // Place les erreurs du serveur : sous le champ concerne (400), sinon dans le bandeau (409...).
  private afficherErreursServeur(e: HttpErrorResponse): void {
    const parChamp = erreursChamps(e);
    let placees = 0;

    for (const [champ, message] of Object.entries(parChamp)) {
      const controle = this.formulaire.get(champ);
      if (controle) {
        controle.setErrors({ serveur: message });
        controle.markAsTouched();
        placees++;
      }
    }

    // Aucune erreur par champ (409 reference deja prise, serveur eteint...) : bandeau general.
    if (placees === 0) {
      this.bandeau.set(messageErreur(e));
    }
  }
}