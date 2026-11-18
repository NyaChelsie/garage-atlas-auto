import { Carburant, StatutVehicule } from './vehicule.model';
import { CategoriePiece } from './piece.model';

// ---------- Vehicules : miroir de VehiculeStatistiques ----------

export interface MarqueCompte {
  marque: string;
  nombre: number;
}

export interface VehiculeStatistiques {
  total: number;
  // Un dictionnaire "statut -> nombre". Partial : une cle peut manquer si elle vaut 0.
  parStatut: Partial<Record<StatutVehicule, number>>;
  parCarburant: Partial<Record<Carburant, number>>;
  topMarques: MarqueCompte[];       // les 5 marques les plus presentes
  kilometrageMoyen: number;
  coutEstimeEnReparation: number;   // somme des couts des vehicules EN_REPARATION
  entresCeMois: number;
}

// ---------- Pieces : miroir de PieceStatistiques ----------

export interface CategorieStat {
  categorie: CategoriePiece;
  nombre: number;
  valeur: number;                   // valeur du stock de cette categorie
}

export interface PieceChere {
  reference: string;
  designation: string;
  prixUnitaire: number;
}

export interface PieceStatistiques {
  nombreReferences: number;
  quantiteTotale: number;
  valeurStock: number;              // somme de (prix x quantite)
  enAlerte: number;
  parCategorie: CategorieStat[];
  // null UNIQUEMENT si aucune piece n'existe : le template devra tester ce cas.
  pieceLaPlusChere: PieceChere | null;
}

// ---------- Clients et interventions : miroir de InterventionStatistiques ----------

export interface ClientDepense {
  clientId: number;
  nom: string;
  montant: number;                  // main-d'oeuvre + pieces
}

export interface PieceUtilisee {
  reference: string;
  designation: string;
  quantite: number;                 // quantite totale utilisee
}

export interface InterventionStatistiques {
  nombreClients: number;
  nombreInterventions: number;
  chiffreAffairesMois: number;
  coutMoyenIntervention: number;
  vehiculesSansIntervention: number;
  topClients: ClientDepense[];      // les 5 clients qui ont le plus depense
  topPieces: PieceUtilisee[];       // les 5 pieces les plus utilisees
}
