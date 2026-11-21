// Component : l'outil qui transforme cette classe en "morceau de page" Angular.
import { Component } from '@angular/core';
// RouterLink : permet d'écrire routerLink="/adresse" dans le HTML (navigation sans recharger la page).
import { RouterLink } from '@angular/router';

@Component({
  // Nom de la balise de ce composant : <app-accueil />
  selector: 'app-accueil',
  // Les outils utilisables dans le HTML de ce composant.
  imports: [RouterLink],
  // Le fichier HTML (l'apparence) et le fichier CSS (les petits styles) de ce composant.
  templateUrl: './accueil.html',
  styleUrl: './accueil.css',
})
export class Accueil {
  // Les 4 cartes de fonctions.
  // "icone" = la liste des tracés du dessin SVG (chaque texte est un trait du dessin).
  // Le HTML parcourra ce tableau avec @for : pour ajouter une carte, on ajoute un bloc ici.
  protected fonctions = [
    {
      titre: 'Clients et véhicules',
      texte: 'Chaque client et ses voitures, avec recherche rapide par nom, téléphone ou immatriculation.',
      icone: [
        'M5.8 8a3.2 3.2 0 1 0 6.4 0a3.2 3.2 0 1 0 -6.4 0',
        'M3 20c0-3.3 2.7-5.5 6-5.5s6 2.2 6 5.5',
        'M16 5.5a3 3 0 0 1 0 5.8M18 14.8c1.8.6 3 2.2 3 4.7',
      ],
    },
    {
      titre: 'Suivi des réparations',
      texte: 'Du dépôt à la livraison : en attente, en réparation, terminé, livré. Le serveur refuse les changements interdits.',
      icone: ['M14.5 6.5a4 4 0 0 0-5 5L3.5 17.5l3 3 6-6a4 4 0 0 0 5-5l-2.5 2.5-2.5-.5-.5-2.5z'],
    },
    {
      titre: 'Stock de pièces',
      texte: 'Le stock baisse à chaque intervention, et une alerte apparaît avant la rupture.',
      icone: ['M21 8l-9-5-9 5 9 5 9-5z', 'M3 8v8l9 5 9-5V8', 'M12 13v8'],
    },
    {
      titre: 'Tableau de bord',
      texte: 'Chiffre d’affaires, coût moyen d’une intervention, valeur du stock : l’essentiel sur un écran.',
      icone: ['M4 20V10M10 20V4M16 20v-7M22 20H2'],
    },
  ];

  // Les 4 colonnes du suivi des véhicules.
  // "couleur" = les classes Tailwind du badge. Elles sont écrites EN ENTIER :
  // c'est ce qui permet à Tailwind de les repérer dans ce fichier et de les fabriquer.
  // Les véhicules sont des exemples écrits à la main, pas les données de l'API.
  protected colonnes = [
    {
      titre: 'En attente',
      couleur: 'bg-amber-100 text-amber-800',
      vehicules: [
        { immat: 'CD-456-EF', detail: 'Toyota Hilux · Jean-Paul Mbarga' },
        { immat: 'IJ-567-KL', detail: 'Nissan Leaf · Marie Ngo' },
      ],
    },
    {
      titre: 'En réparation',
      couleur: 'bg-orange-100 text-orange-800',
      vehicules: [
        { immat: 'AB-123-CD', detail: 'Toyota Corolla · Jean-Paul Mbarga' },
        { immat: 'KL-890-MN', detail: 'Hyundai Tucson · Patrick Tchoumi' },
      ],
    },
    {
      titre: 'Terminé',
      couleur: 'bg-emerald-100 text-emerald-800',
      vehicules: [
        { immat: 'GH-234-IJ', detail: 'Peugeot 308 · Marie Ngo' },
        { immat: 'MN-111-OP', detail: 'Kia Sportage · Alice Fotso' },
      ],
    },
    {
      titre: 'Livré',
      couleur: 'bg-gray-200 text-gray-700',
      vehicules: [{ immat: 'EF-789-GH', detail: 'Renault Clio · Jean-Paul Mbarga' }],
    },
  ];

  // Fait défiler la page, en douceur, jusqu'à la section dont l'id est donné.
  // "?." : si aucun élément n'a cet id, on ne fait rien (pas d'erreur).
  protected defiler(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  }
}
