import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  protected liens = [
    { chemin: '/tableau-de-bord', libelle: 'Tableau de bord' },
    { chemin: '/vehicules', libelle: 'Véhicules' },
    { chemin: '/pieces', libelle: 'Pièces' },
    { chemin: '/clients', libelle: 'Clients' },
  ];

  // Menu ouvert ou fermé (utile seulement sur petit écran).
  protected ouvert = signal(false);

  protected basculer(): void {
    this.ouvert.update((v) => !v);
  }

  protected fermer(): void {
    this.ouvert.set(false);
  }
}
