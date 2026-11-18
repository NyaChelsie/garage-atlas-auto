// Liste FERMEE des categories : miroir de l'enum CategoriePiece cote Spring Boot.
export type CategoriePiece =
  'MOTEUR' | 'FREINAGE' | 'SUSPENSION' | 'ELECTRICITE' | 'CARROSSERIE' | 'ENTRETIEN';

// Les deux seules operations de stock : miroir de l'enum OperationStock.
export type OperationStock = 'ENTREE' | 'SORTIE';

// Ce que l'API RENVOIE : miroir de PieceReponse cote Spring Boot.
export interface Piece {
  id: number;
  reference: string;          // format PCE-0001
  designation: string;
  categorie: CategoriePiece;
  fabricant: string;
  prixUnitaire: number;       // BigDecimal Java arrive en nombre dans le JSON
  quantiteStock: number;
  seuilAlerte: number;
  dateAchat: string;          // texte ISO : "2026-09-20"
  // Calcule par le SERVEUR : quantiteStock <= seuilAlerte.
  // Le front n'a pas a refaire le calcul, il affiche seulement.
  enAlerte: boolean;
}

// Ce que le front ENVOIE (POST et PUT) : miroir de PieceRequete.
// Pas d'id, et pas de enAlerte : c'est le serveur qui le calcule.
export interface PieceRequete {
  reference: string;
  designation: string;
  categorie: CategoriePiece;
  fabricant: string;
  prixUnitaire: number;
  quantiteStock: number;
  seuilAlerte: number;
  dateAchat: string;
}

// Corps du PATCH /api/pieces/{id}/stock : miroir de StockRequete.
// Exemple : { operation: 'SORTIE', quantite: 2 }
export interface StockRequete {
  operation: OperationStock;
  quantite: number;
}
