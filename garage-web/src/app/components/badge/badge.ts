import { Component, computed, input } from '@angular/core';

import { StatutVehicule } from '../../models/vehicule.model';

// Un dictionnaire "statut -> classes Tailwind" : une couleur par statut.
// Record<StatutVehicule, string> : TypeScript EXIGE une entree pour chacun des 4 statuts.
// Si on ajoutait un statut plus tard sans lui donner de couleur, ce serait une erreur.
// Les classes sont ecrites en entier : Tailwind ne repere que les classes qu'il lit tel quel.
const STYLES: Record<StatutVehicule, string> = {
  EN_ATTENTE: 'bg-amber-100 text-amber-800',
  EN_REPARATION: 'bg-blue-100 text-blue-800',
  TERMINE: 'bg-green-100 text-green-800',
  LIVRE: 'bg-gray-200 text-gray-700',
};

// Le texte affiche a l'utilisateur, en francais lisible (et non EN_REPARATION).
const LIBELLES: Record<StatutVehicule, string> = {
  EN_ATTENTE: 'En attente',
  EN_REPARATION: 'En réparation',
  TERMINE: 'Terminé',
  LIVRE: 'Livré',
};

@Component({
  // Balise a utiliser : <app-badge [statut]="vehicule.statut" />
  selector: 'app-badge',
  imports: [],
  templateUrl: './badge.html',
  styleUrl: './badge.css',
})
export class Badge {
  // input.required : le parent DOIT fournir le statut a afficher.
  statut = input.required<StatutVehicule>();

  // computed : une valeur calculee a partir d'un signal ; elle se met a jour toute seule
  // quand le statut change.
  protected classes = computed(() => STYLES[this.statut()]);
  protected libelle = computed(() => LIBELLES[this.statut()]);
}