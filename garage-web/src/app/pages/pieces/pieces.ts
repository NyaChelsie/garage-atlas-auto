import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

import { Pagination } from '../../components/pagination/pagination';
import { messageErreur } from '../../models/erreur.model';
import { CategoriePiece, OperationStock, Piece } from '../../models/piece.model';
import { NotificationService } from '../../services/notification.service';
import { PieceService } from '../../services/piece.service';

// Les choix du filtre : la valeur envoyee au serveur + le texte affiche.
const CATEGORIES: { valeur: CategoriePiece; libelle: string }[] = [
  { valeur: 'MOTEUR', libelle: 'Moteur' },
  { valeur: 'FREINAGE', libelle: 'Freinage' },
  { valeur: 'SUSPENSION', libelle: 'Suspension' },
  { valeur: 'ELECTRICITE', libelle: 'Électricité' },
  { valeur: 'CARROSSERIE', libelle: 'Carrosserie' },
  { valeur: 'ENTRETIEN', libelle: 'Entretien' },
];

@Component({
  selector: 'app-pieces',
  imports: [RouterLink, ReactiveFormsModule, Pagination],
  templateUrl: './pieces.html',
  styleUrl: './pieces.css',
})
export class Pieces {
  private fb = inject(NonNullableFormBuilder);
  private pieceService = inject(PieceService);
  private notification = inject(NotificationService);

  private readonly TAILLE = 10;

  // Liste des categories, lue par le HTML pour remplir le filtre.
  protected categories = CATEGORIES;

  // ---------- L'etat de l'ecran ----------
  protected pieces = signal<Piece[]>([]);
  protected page = signal(0);
  protected totalPages = signal(0);
  protected totalElements = signal(0);
  protected chargement = signal(true);
  protected erreur = signal<string | null>(null);

  // ---------- Le panneau de mouvement de stock ----------
  // Id de la piece dont le panneau est ouvert (null = aucun panneau ouvert).
  // Un seul panneau a la fois : ouvrir un autre ferme le precedent.
  protected pieceOuverte = signal<number | null>(null);
  protected envoiEnCours = signal(false);          // bouton desactive pendant l'envoi
  protected erreurMouvement = signal<string | null>(null);   // message du serveur (409...)

  // Le mini-formulaire du panneau : un formulaire reactif type, comme les autres.
  protected mouvement = this.fb.group({
    operation: this.fb.control<OperationStock>('ENTREE'),
    // Entier d'au moins 1 : chiffres seulement, et jamais 0.
    quantite: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.pattern(/^\d+$/),
      Validators.min(1),
    ]),
  });

  // ---------- Les filtres courants ----------
  private recherche = '';
  private categorie: CategoriePiece | undefined;   // undefined = pas de filtre
  private saisie$ = new Subject<string>();

  constructor() {
    // 300 ms de pause avant de chercher : une seule requete par recherche.
    this.saisie$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((texte) => {
        this.recherche = texte;
        this.page.set(0);
        this.charger();
      });

    this.charger();
  }

  protected rechercher(texte: string): void {
    this.saisie$.next(texte);
  }

  // Appelee quand l'utilisateur choisit une categorie. '' = "Toutes les categories".
  protected filtrerCategorie(valeur: string): void {
    // find : retrouve un vrai CategoriePiece dans la liste (ou undefined).
    this.categorie = CATEGORIES.find((c) => c.valeur === valeur)?.valeur;
    this.page.set(0);   // un filtre change : retour a la page 1
    this.charger();
  }

  protected changerPage(numero: number): void {
    this.page.set(numero);
    this.charger();
  }

  protected charger(): void {
    // La liste va changer : on ferme le panneau de stock (il pourrait viser une piece disparue).
    this.fermerMouvement();
    this.chargement.set(true);
    this.erreur.set(null);

    this.pieceService.lister(this.page(), this.TAILLE, this.recherche, this.categorie).subscribe({
      next: (resultat) => {
        this.pieces.set(resultat.contenu);
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

  // ---------- Mouvements de stock ----------

  // Ouvre (ou referme) le panneau de stock d'une piece.
  protected ouvrirMouvement(piece: Piece): void {
    // Un second clic sur la meme piece referme le panneau.
    if (this.pieceOuverte() === piece.id) {
      this.fermerMouvement();
      return;
    }
    // reset() remet le formulaire a ses valeurs de depart : Entree, quantite vide.
    this.mouvement.reset();
    this.erreurMouvement.set(null);
    this.pieceOuverte.set(piece.id);
  }

  protected fermerMouvement(): void {
    this.pieceOuverte.set(null);
    this.erreurMouvement.set(null);
  }

  // Le message a afficher sous le champ quantite (ou null s'il n'y a rien a dire).
  protected erreurQuantite(): string | null {
    const controle = this.mouvement.controls.quantite;
    if (!controle.touched || !controle.errors) {
      return null;
    }
    if (controle.errors['required']) {
      return 'La quantité est obligatoire';
    }
    // pattern (decimal ou negatif) et min (zero) : meme conseil pour l'utilisateur.
    return "La quantité doit être un entier d'au moins 1";
  }

  protected enregistrerMouvement(piece: Piece): void {
    if (this.mouvement.invalid) {
      this.mouvement.markAllAsTouched();
      return;
    }

    const { operation, quantite } = this.mouvement.getRawValue();
    // Garde-fou pour TypeScript : un formulaire valide n'a jamais une quantite vide.
    if (quantite === null) {
      return;
    }

    this.envoiEnCours.set(true);
    this.erreurMouvement.set(null);

    // Number(...) : le serveur attend un NOMBRE, jamais un texte.
    this.pieceService.mouvementStock(piece.id, operation, Number(quantite)).subscribe({
      next: () => {
        this.envoiEnCours.set(false);
        this.notification.succes(
          operation === 'ENTREE' ? 'Entrée de stock enregistrée.' : 'Sortie de stock enregistrée.'
        );
        // On relit la liste : le stock et l'etiquette "Alerte" viennent du serveur.
        // charger() referme aussi le panneau.
        this.charger();
      },
      // 409 "stock insuffisant" : le panneau reste ouvert avec le message du serveur.
      error: (e: HttpErrorResponse) => {
        this.envoiEnCours.set(false);
        this.erreurMouvement.set(messageErreur(e));
      },
    });
  }

  protected supprimer(piece: Piece): void {
    if (!confirm(`Supprimer la pièce ${piece.reference} (${piece.designation}) ?`)) {
      return;
    }

    this.pieceService.supprimer(piece.id).subscribe({
      next: () => {
        this.notification.succes('Pièce supprimée.');
        // Derniere ligne d'une page autre que la premiere : on recule d'une page.
        if (this.pieces().length === 1 && this.page() > 0) {
          this.page.update((p) => p - 1);
        }
        this.charger();
      },
      // 409 : la piece est utilisee dans une intervention. On affiche le message du serveur.
      error: (e: HttpErrorResponse) => this.notification.erreur(messageErreur(e)),
    });
  }

  // Donne le libelle francais d'une categorie (ELECTRICITE devient "Electricite").
  protected libelleCategorie(valeur: CategoriePiece): string {
    return CATEGORIES.find((c) => c.valeur === valeur)?.libelle ?? valeur;
  }

  // Met un montant en forme : 15000 devient "15 000 FCFA".
  protected fcfa(montant: number): string {
    return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
  }
}