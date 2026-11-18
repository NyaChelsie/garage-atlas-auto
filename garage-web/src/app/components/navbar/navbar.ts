import { Component } from '@angular/core';
// RouterLink : transforme un lien en navigation Angular (sans recharger la page).
// RouterLinkActive : ajoute des classes CSS au lien dont la route est la route courante.
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  // Balise a placer dans app.html : <app-navbar />
  selector: 'app-navbar',
  // Les deux outils doivent etre declares ici pour etre utilisables dans le HTML.
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  // Les liens du menu : un seul tableau, le HTML le parcourt avec @for.
  // Pour ajouter une page au menu, il suffit d'ajouter une ligne ici.
  protected liens = [
    { chemin: '/tableau-de-bord', libelle: 'Tableau de bord' },
    { chemin: '/vehicules', libelle: 'Véhicules' },
    { chemin: '/pieces', libelle: 'Pièces' },
    { chemin: '/clients', libelle: 'Clients' },
  ];
}
