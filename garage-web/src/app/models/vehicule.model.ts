// Un "type union" : une liste FERMEE de valeurs autorisees.
// C'est l'equivalent TypeScript d'un enum Java. Si tu ecris 'ESENCE' (faute de frappe),
// TypeScript refuse tout de suite, avant meme de lancer l'application.
export type Carburant = 'ESSENCE' | 'DIESEL' | 'ELECTRIQUE' | 'HYBRIDE';
export type StatutVehicule = 'EN_ATTENTE' | 'EN_REPARATION' | 'TERMINE' | 'LIVRE';

// Ce que l'API RENVOIE : miroir de VehiculeReponse cote Spring Boot.
export interface Vehicule {
  id: number;
  immatriculation: string;
  marque: string;
  modele: string;
  annee: number;
  kilometrage: number;
  carburant: Carburant;
  statut: StatutVehicule;
  dateEntree: string;        // texte ISO : "2026-01-01"
  coutEstime: number;        // BigDecimal Java arrive en nombre dans le JSON
  panne: string | null;      // facultatif
  clientId: number;          // la cle etrangere : l'API ne renvoie pas le client entier
  clientNom: string;         // "Jean-Paul Mbarga" : pratique pour l'affichage
}

// Ce que le front ENVOIE (POST et PUT) : miroir de VehiculeRequete cote Spring Boot.
// Pas d'id, et pas de clientNom : le serveur le calcule lui-meme.
export interface VehiculeRequete {
  clientId: number;          // obligatoire : designe le proprietaire
  immatriculation: string;   // format AB-123-CD
  marque: string;
  modele: string;
  annee: number;
  kilometrage: number;
  carburant: Carburant;
  // "?" = champ facultatif : le serveur impose EN_ATTENTE a la creation.
  // Le statut change ensuite par PATCH /statut, pas par ce formulaire.
  statut?: StatutVehicule;
  dateEntree: string;
  coutEstime: number;
  panne: string | null;
}

// Les statuts autorises depuis chaque statut : miroir du tableau TRANSITIONS
// de VehiculeService cote Spring Boot.
// Record<StatutVehicule, ...> : TypeScript EXIGE une entree pour chacun des 4 statuts.
// Si on en oubliait un, le compilateur le signalerait tout de suite.
export const TRANSITIONS: Record<StatutVehicule, StatutVehicule[]> = {
  EN_ATTENTE: ['EN_REPARATION'],
  EN_REPARATION: ['TERMINE'],
  TERMINE: ['LIVRE', 'EN_REPARATION'],
  LIVRE: [],                 // terminus : plus aucun changement possible
};

// Le texte francais de chaque statut, pour l'affichage.
export const LIBELLES_STATUT: Record<StatutVehicule, string> = {
  EN_ATTENTE: 'En attente',
  EN_REPARATION: 'En réparation',
  TERMINE: 'Terminé',
  LIVRE: 'Livré',
};