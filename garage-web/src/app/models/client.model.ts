// Ce que l'API RENVOIE : miroir de ClientReponse cote Spring Boot.
export interface Client {
  id: number;
  nom: string;
  prenom: string;
  telephone: string;
  // "string | null" : le champ existe toujours dans le JSON, mais peut valoir null.
  // Le sujet dit que l'email et la ville sont facultatifs.
  email: string | null;
  ville: string | null;
  // Les dates LocalDate de Java arrivent en texte ISO : "2025-10-11".
  dateInscription: string;
}

// Ce que le front ENVOIE (POST et PUT) : miroir de ClientRequete.
// Pas d'id : il est genere par la base de donnees.
export interface ClientRequete {
  nom: string;
  prenom: string;
  telephone: string;
  email: string | null;
  ville: string | null;
  dateInscription: string;
}
