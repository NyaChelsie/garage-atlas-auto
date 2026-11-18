import { Component, input, output } from '@angular/core';

@Component({
  // Balise a utiliser dans les pages : <app-pagination ... />
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.html',
  styleUrl: './pagination.css',
})
export class Pagination {
  // input.required : valeurs que la page parente DOIT fournir.
  // page : numero de la page courante (la premiere vaut 0, comme dans l'API).
  page = input.required<number>();
  totalPages = input.required<number>();
  totalElements = input.required<number>();

  // output : le composant "previent" la page parente quand l'utilisateur change de page.
  // Il envoie le numero de la nouvelle page ; c'est la page parente qui recharge les donnees.
  changer = output<number>();
}
