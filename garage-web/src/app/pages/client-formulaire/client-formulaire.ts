import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
// Formulaires REACTIFS : la structure du formulaire est decrite dans le code TypeScript.
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ClientRequete } from '../../models/client.model';
import { erreursChamps, messageErreur } from '../../models/erreur.model';
import { ClientService } from '../../services/client.service';
import { NotificationService } from '../../services/notification.service';
import { aujourdhui, pasDansLeFutur } from '../../validators/dates.validator';

// Les noms des champs : TypeScript refuse toute faute de frappe.
type ChampClient = 'nom' | 'prenom' | 'telephone' | 'email' | 'ville' | 'dateInscription';

@Component({
  selector: 'app-client-formulaire',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './client-formulaire.html',
  styleUrl: './client-formulaire.css',
})
export class ClientFormulaire {
  private fb = inject(NonNullableFormBuilder);
  private clientService = inject(ClientService);
  private notification = inject(NotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Mode : l'adresse /clients/3/modifier contient un id, /clients/nouveau n'en contient pas.
  protected id: number | null = this.lireId();
  protected modification = this.id !== null;

  protected envoiEnCours = signal(false);        // bouton desactive pendant l'envoi
  protected chargement = signal(this.modification); // on charge le client en mode modification
  protected bandeau = signal<string | null>(null);  // erreur generale (409, 500, serveur eteint)

  // Date du jour : sert de limite maximale au champ date.
  protected aujourdhui = aujourdhui();

  // Le formulaire : memes regles que ClientRequete.java cote serveur.
  // NonNullableFormBuilder : un champ vide vaut '' et jamais null (formulaire "type").
  protected formulaire = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(60)]],
    prenom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(60)]],
    // 9 chiffres, le premier est un 6.
    telephone: ['', [Validators.required, Validators.pattern(/^6\d{8}$/)]],
    // Facultatifs : pas de "required". Validators.email accepte le champ vide.
    email: ['', [Validators.email, Validators.maxLength(100)]],
    ville: ['', [Validators.maxLength(60)]],
    dateInscription: ['', [Validators.required, pasDansLeFutur]],
  });

  constructor() {
    // En modification : on charge le client puis on remplit le formulaire.
    if (this.id !== null) {
      this.clientService.lire(this.id).subscribe({
        next: (client) => {
          // patchValue : remplit les champs. email et ville peuvent etre null -> ''.
          this.formulaire.patchValue({
            nom: client.nom,
            prenom: client.prenom,
            telephone: client.telephone,
            email: client.email ?? '',
            ville: client.ville ?? '',
            dateInscription: client.dateInscription,
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
  // On n'affiche qu'APRES que l'utilisateur a touche le champ.
  protected erreur(champ: ChampClient): string | null {
    const controle = this.formulaire.controls[champ];
    if (!controle.touched || !controle.errors) {
      return null;
    }
    const e = controle.errors;
    // L'erreur renvoyee par le serveur passe en premier.
    if (e['serveur']) return String(e['serveur']);
    if (e['required']) return 'Ce champ est obligatoire';
    if (e['minlength']) return 'Minimum 2 caractères';
    if (e['maxlength']) return `Maximum ${e['maxlength'].requiredLength} caractères`;
    if (e['pattern']) return 'Le téléphone doit contenir 9 chiffres et commencer par 6';
    if (e['email']) return "Format d'email invalide";
    if (e['futur']) return 'La date ne peut pas être dans le futur';
    return 'Valeur invalide';
  }

  protected enregistrer(): void {
    // Formulaire invalide : on "touche" tous les champs pour afficher leurs erreurs.
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.envoiEnCours.set(true);
    this.bandeau.set(null);

    const valeurs = this.formulaire.getRawValue();
    // Email et ville facultatifs : un champ vide est envoye comme null.
    const requete: ClientRequete = {
      nom: valeurs.nom.trim(),
      prenom: valeurs.prenom.trim(),
      telephone: valeurs.telephone.trim(),
      email: valeurs.email.trim() === '' ? null : valeurs.email.trim(),
      ville: valeurs.ville.trim() === '' ? null : valeurs.ville.trim(),
      dateInscription: valeurs.dateInscription,
    };

    // Creation ou modification, selon le mode.
    const appel =
      this.id === null
        ? this.clientService.creer(requete)
        : this.clientService.modifier(this.id, requete);

    appel.subscribe({
      next: () => {
        this.notification.succes(this.id === null ? 'Client créé.' : 'Client modifié.');
        // Navigation RELATIVE vers la liste : on remonte d'un cran depuis l'adresse courante.
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
        // setErrors : pose l'erreur ; elle disparait toute seule des que l'utilisateur modifie le champ.
        controle.setErrors({ serveur: message });
        controle.markAsTouched();
        placees++;
      }
    }

    // Aucune erreur par champ (409 doublon de telephone, serveur eteint...) : bandeau general.
    if (placees === 0) {
      this.bandeau.set(messageErreur(e));
    }
  }
}