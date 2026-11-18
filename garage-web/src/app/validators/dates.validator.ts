import { AbstractControl, ValidationErrors } from '@angular/forms';

// La date du jour au format "2026-11-18", dans le fuseau de l'utilisateur.
// Ce format texte se compare directement avec le champ <input type="date">.
export function aujourdhui(): string {
  const d = new Date();
  const mois = String(d.getMonth() + 1).padStart(2, '0');
  const jour = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mois}-${jour}`;
}

// Un "validateur" : une fonction qui renvoie null si tout va bien,
// ou un objet d'erreur si la regle n'est pas respectee.
// Les dates ISO ("2026-11-18") se comparent comme du texte : l'ordre alphabetique
// est aussi l'ordre chronologique.
export function pasDansLeFutur(control: AbstractControl): ValidationErrors | null {
  const valeur = String(control.value ?? '');
  // Champ vide : la regle "obligatoire" s'en occupe, pas celle-ci.
  if (valeur === '') {
    return null;
  }
  return valeur > aujourdhui() ? { futur: true } : null;
}