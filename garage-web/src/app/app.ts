import { Component } from '@angular/core';
// RouterOutlet = l'emplacement ou Angular affiche la page choisie par la route.
import { RouterOutlet } from '@angular/router';

import { Navbar } from './components/navbar/navbar';
import { Notification } from './components/notification/notification';

@Component({
  // Nom de la balise HTML qui affiche ce composant (utilisee dans index.html).
  selector: 'app-root',
  // Les composants utilises dans app.html doivent etre declares ici.
  imports: [RouterOutlet, Navbar, Notification],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
