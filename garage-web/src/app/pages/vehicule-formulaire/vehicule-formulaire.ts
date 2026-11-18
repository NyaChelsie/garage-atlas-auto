import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Client } from '../../models/client.model';
import { erreursChamps, messageErreur } from '../../models/erreur.model';
import { Carburant, VehiculeRequete } from '../../models/vehicule.model';
import { ClientService } from '../../services/client.service';
import { NotificationService } from '../../services/notification.service';
import { VehiculeService } from '../../services/vehicule.service';
import { aujourdhui, pasDansLeFutur } from '../../validators/dates.validator';

// Les noms des champs : TypeScript refuse toute faute de frappe.
type ChampVehicule =
  | 'clientId'
  | 'immatriculation'
  | 'marque'
  | 'modele'
  | 'annee'
  | 'kilometrage'
  | 'carburant'
  | 'dateEntree'
  | 'coutEstime'
  | 'panne';

// L'annee maximale est CALCULEE (annee en cours), jamais ecrite en dur : exigence du sujet.
const ANNEE_COURANTE = new Date().getFullYear();

// Les choix de la liste deroulante : valeur envoyee au serveur + texte affiche.
const CARBURANTS: { valeur: Carburant; libelle: string }[] = [
  { valeur: 'ESSENCE', libelle: 'Essence' },
  { valeur: 'DIESEL', libelle: 'Diesel' },
  { valeur: 'ELECTRIQUE', libelle: 'Électrique' },
  { valeur: 'HYBRIDE', libelle: 'Hybride' },
];

@Component({
  selector: 'app-vehicule-formulaire',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './vehicule-formulaire.html',
  styleUrl: './vehicule-formulaire.css',
})
export class VehiculeFormulaire {
  private fb = inject(NonNullableFormBuilder);
  private vehiculeService = inject(VehiculeService);
  private clientService = inject(ClientService);
  private notification = inject(NotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Mode : /vehicules/3/modifier contient un id, /vehicules/nouveau n'en contient pas.
  protected id: number | null = this.lireId();
  protected modification = this.id !== null;

  protected envoiEnCours = signal(false);
  protected chargement = signal(this.modification);
  protected bandeau = signal<string | null>(null);

  // Les clients de la liste deroulante, charges depuis l'API.
  protected clients = signal<Client[]>([]);

  // Lus par le HTML.
  protected aujourdhui = aujourdhui();
  protected carburants = CARBURANTS;
  protected anneeCourante = ANNEE_COURANTE;

  // Le formulaire : memes regles que le serveur.
  protected formulaire = this.fb.group({
    // null = "aucun client choisi" ; required refuse ce cas.
    clientId: this.fb.control<number | null>(null, Validators.required),
    // Format AB-123-CD : 2 lettres MAJUSCULES, 3 chiffres, 2 lettres MAJUSCULES.
    immatriculation: ['', [Validators.required, Validators.pattern(/^[A-Z]{2}-\d{3}-[A-Z]{2}$/)]],
    marque: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(40)]],
    modele: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(40)]],
    // Entier de 2000 a l'annee en cours.
    annee: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.pattern(/^\d+$/),
      Validators.min(2000),
      Validators.max(ANNEE_COURANTE),
    ]),
    // Entier positif ou nul : uniquement des chiffres.
    kilometrage: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.pattern(/^\d+$/),
    ]),
    carburant: this.fb.control<Carburant | ''>('', Validators.required),
    dateEntree: ['', [Validators.required, pasDansLeFutur]],
    // 0 par defaut : "pas encore estime". Jamais negatif.
    coutEstime: this.fb.control<number | null>(0, [Validators.required, Validators.min(0)]),
    // Facultative, 500 caracteres maximum.
    panne: ['', [Validators.maxLength(500)]],
  });

  constructor() {
    // La liste deroulante : on charge les clients (jusqu'a 200, largement suffisant ici).
    this.clientService.lister(0, 200).subscribe({
      next: (resultat) => this.clients.set(resultat.contenu),
      error: (e: HttpErrorResponse) => this.bandeau.set(messageErreur(e)),
    });

    // En modification : on charge le vehicule puis on remplit le formulaire.
    if (this.id !== null) {
      this.vehiculeService.lire(this.id).subscribe({
        next: (vehicule) => {
          this.formulaire.patchValue({
            clientId: vehicule.clientId,
            immatriculation: vehicule.immatriculation,
            marque: vehicule.marque,
            modele: vehicule.modele,
            annee: vehicule.annee,
            kilometrage: vehicule.kilometrage,
            carburant: vehicule.carburant,
            dateEntree: vehicule.dateEntree,
            coutEstime: vehicule.coutEstime,
            panne: vehicule.panne ?? '',
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
  protected erreur(champ: ChampVehicule): string | null {
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
      if (champ === 'immatriculation') return 'Format attendu : AB-123-CD';
      return 'Nombre entier positif ou nul attendu';
    }
    if (e['min']) {
      return champ === 'annee'
        ? "L'année doit être au moins 2000"
        : 'Le coût estimé ne peut pas être négatif';
    }
    if (e['max']) return `L'année ne peut pas dépasser ${ANNEE_COURANTE}`;
    if (e['futur']) return 'La date ne peut pas être dans le futur';
    return 'Valeur invalide';
  }

  protected enregistrer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    const valeurs = this.formulaire.getRawValue();
    // Garde-fou pour TypeScript : apres ce test, ces champs ne sont ni '' ni null.
    if (
      valeurs.clientId === null ||
      valeurs.annee === null ||
      valeurs.kilometrage === null ||
      valeurs.carburant === '' ||
      valeurs.coutEstime === null
    ) {
      return;
    }

    this.envoiEnCours.set(true);
    this.bandeau.set(null);

    const panne = valeurs.panne.trim();
    const requete: VehiculeRequete = {
      clientId: valeurs.clientId,
      immatriculation: valeurs.immatriculation.trim(),
      marque: valeurs.marque.trim(),
      modele: valeurs.modele.trim(),
      annee: Number(valeurs.annee),
      kilometrage: Number(valeurs.kilometrage),
      carburant: valeurs.carburant,
      dateEntree: valeurs.dateEntree,
      coutEstime: Number(valeurs.coutEstime),
      // Panne facultative : un champ vide est envoye comme null.
      panne: panne === '' ? null : panne,
    };

    const appel =
      this.id === null
        ? this.vehiculeService.creer(requete)
        : this.vehiculeService.modifier(this.id, requete);

    appel.subscribe({
      next: () => {
        this.notification.succes(this.id === null ? 'Véhicule créé.' : 'Véhicule modifié.');
        // Navigation relative vers la liste : meme principe que pour les clients.
        this.router.navigate([this.id === null ? '..' : '../..'], { relativeTo: this.route });
      },
      error: (e: HttpErrorResponse) => {
        this.envoiEnCours.set(false);
        this.afficherErreursServeur(e);
      },
    });
  }

  // Place les erreurs du serveur : sous le champ concerne, sinon dans le bandeau.
  private afficherErreursServeur(e: HttpErrorResponse): void {
    const parChamp = erreursChamps(e);
    let placees = 0;

    // Cas 400 : le serveur donne une liste "champ -> message".
    for (const [champ, message] of Object.entries(parChamp)) {
      const controle = this.formulaire.get(champ);
      if (controle) {
        controle.setErrors({ serveur: message });
        controle.markAsTouched();
        placees++;
      }
    }

    if (placees === 0) {
      const message = messageErreur(e);
      // Cas 409 : le serveur donne un seul message, sans nom de champ.
      // Le sujet veut l'erreur SOUS le champ : on le place sous l'immatriculation.
      if (e.status === 409 && /immatriculation/i.test(message)) {
        const controle = this.formulaire.controls.immatriculation;
        controle.setErrors({ serveur: message });
        controle.markAsTouched();
      } else {
        // Autre cas (serveur eteint, 500...) : bandeau general.
        this.bandeau.set(message);
      }
    }
  }
}