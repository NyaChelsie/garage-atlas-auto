// Une "interface" decrit la FORME d'un objet : quels champs, de quel type.
// Elle n'existe qu'a la compilation : aucun code n'est genere dans le navigateur.
//
// <T> : "type quelconque". Une seule interface sert pour les clients, les vehicules
// et les pieces : Page<Vehicule>, Page<Piece>, Page<Client>.
// Elle reflete exactement le PageReponse<T> de Spring Boot.
export interface Page<T> {
  contenu: T[];          // les elements de la page courante (T[] = tableau de T)
  page: number;          // numero de la page, la premiere vaut 0
  taille: number;        // nombre d'elements par page
  totalElements: number; // nombre total d'elements, toutes pages confondues
  totalPages: number;    // nombre total de pages
}
