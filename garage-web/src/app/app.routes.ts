import { Routes } from '@angular/router';

// Une route associe une ADRESSE du navigateur a une PAGE.
// loadComponent + import() : la page n'est chargee que lorsqu'on la visite (chargement
// "paresseux"), ce qui garde le demarrage de l'application leger.
// Les chemins n'ont pas de "/" au debut : Angular l'ajoute tout seul.
export const routes: Routes = [
  // Adresse vide "/" : on redirige vers le tableau de bord.
  // pathMatch: 'full' = "seulement si l'adresse est EXACTEMENT vide".
  { path: '', title: 'Accueil', pathMatch: 'full', loadComponent: () => import('./pages/accueil/accueil').then(m => m.Accueil) },

  {
    path: 'tableau-de-bord',
    title: 'Tableau de bord',
    loadComponent: () => import('./pages/tableau-de-bord/tableau-de-bord').then(m => m.TableauDeBord),
  },

  // ---------- Vehicules ----------
  {
    path: 'vehicules',
    title: 'Vehicules',
    loadComponent: () => import('./pages/vehicules/vehicules').then(m => m.Vehicules),
  },
  // IMPORTANT : "nouveau" doit etre declare AVANT ":id".
  // Sinon Angular prendrait le mot "nouveau" pour un identifiant.
  {
    path: 'vehicules/nouveau',
    title: 'Nouveau vehicule',
    loadComponent: () => import('./pages/vehicule-formulaire/vehicule-formulaire').then(m => m.VehiculeFormulaire),
  },
  {
    // ":id" est un parametre : /vehicules/3/modifier donne id = 3.
    path: 'vehicules/:id/modifier',
    title: 'Modifier un vehicule',
    loadComponent: () => import('./pages/vehicule-formulaire/vehicule-formulaire').then(m => m.VehiculeFormulaire),
  },
  {
    // La fiche d'un vehicule : /vehicules/3
    path: 'vehicules/:id',
    title: 'Fiche vehicule',
    loadComponent: () => import('./pages/fiche-vehicule/fiche-vehicule').then(m => m.FicheVehicule),
  },

  // ---------- Pieces ----------
  {
    path: 'pieces',
    title: 'Pieces',
    loadComponent: () => import('./pages/pieces/pieces').then(m => m.Pieces),
  },
  {
    path: 'pieces/nouveau',
    title: 'Nouvelle piece',
    loadComponent: () => import('./pages/piece-formulaire/piece-formulaire').then(m => m.PieceFormulaire),
  },
  {
    path: 'pieces/:id/modifier',
    title: 'Modifier une piece',
    loadComponent: () => import('./pages/piece-formulaire/piece-formulaire').then(m => m.PieceFormulaire),
  },

  // ---------- Clients ----------
  {
    path: 'clients',
    title: 'Clients',
    loadComponent: () => import('./pages/clients/clients').then(m => m.Clients),
  },
  {
    path: 'clients/nouveau',
    title: 'Nouveau client',
    loadComponent: () => import('./pages/client-formulaire/client-formulaire').then(m => m.ClientFormulaire),
  },
  {
    path: 'clients/:id/modifier',
    title: 'Modifier un client',
    loadComponent: () => import('./pages/client-formulaire/client-formulaire').then(m => m.ClientFormulaire),
  },

  // Joker "**" : toute adresse inconnue arrive ici. Il doit etre la DERNIERE route,
  // car Angular lit la liste de haut en bas et s'arrete a la premiere qui correspond.
  {
    path: '**',
    title: 'Page introuvable',
    loadComponent: () => import('./pages/page-introuvable/page-introuvable').then(m => m.PageIntrouvable),
  },
];
