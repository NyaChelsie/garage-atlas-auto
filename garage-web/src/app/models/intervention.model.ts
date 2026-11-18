// Liste FERMEE des types : miroir de l'enum TypeIntervention cote Spring Boot.
export type TypeIntervention =
  'VIDANGE' | 'FREINS' | 'MOTEUR' | 'ELECTRICITE' | 'CARROSSERIE' | 'DIAGNOSTIC';

// Une ligne de facture : miroir de LigneReponse.
// Le prix est COPIE par le serveur a l'ajout : il ne change jamais ensuite.
export interface LigneIntervention {
  id: number;
  pieceId: number;
  pieceReference: string;      // "PCE-0001" : pratique pour l'affichage
  pieceDesignation: string;
  quantite: number;
  prixUnitaire: number;        // prix de la piece AU MOMENT de l'ajout
  sousTotal: number;           // calcule par le serveur : prixUnitaire x quantite
}

// Ce que l'API RENVOIE : miroir de InterventionReponse.
// Pas de vehicule entier (pas de boucle infinie) : seulement vehiculeId.
export interface Intervention {
  id: number;
  vehiculeId: number;
  type: TypeIntervention;
  description: string;
  dateDebut: string;           // texte ISO : "2026-11-01"
  dateFin: string | null;      // null = intervention encore en cours
  mainOeuvre: number | null;   // facultative cote serveur
  lignes: LigneIntervention[]; // peut etre vide : [] si aucune piece utilisee
  total: number;               // calcule par le serveur : mainOeuvre + somme des sous-totaux
}

// Ce que le front ENVOIE (POST et PUT) : miroir de InterventionRequete.
// vehiculeId : sert a la creation. A la modification, le serveur l'ignore
// (l'intervention reste sur son vehicule), mais il doit quand meme etre envoye.
export interface InterventionRequete {
  vehiculeId: number;
  type: TypeIntervention;
  description: string;         // 5 a 300 caracteres
  dateDebut: string;
  dateFin: string | null;      // facultative, pas avant dateDebut
  mainOeuvre: number | null;   // facultative, 0 minimum
}

// Corps du POST /api/interventions/{id}/lignes : miroir de LigneRequete.
// Exemple : { pieceId: 3, quantite: 2 }
// Pas de prix : c'est le serveur qui copie le prix actuel de la piece.
export interface LigneRequete {
  pieceId: number;
  quantite: number;            // 1 minimum
}
