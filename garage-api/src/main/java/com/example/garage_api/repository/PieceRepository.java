package com.example.garage_api.repository;

import com.example.garage_api.entity.Piece;
import com.example.garage_api.enums.CategoriePiece;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Page : le resultat d'une recherche paginee (elements + nombre total).
import org.springframework.data.domain.Page;
// Pageable : la demande de pagination (numero de page, taille, tri).
import org.springframework.data.domain.Pageable;
// JpaRepository : fournit deja save, findById, findAll, delete, count...
import org.springframework.data.jpa.repository.JpaRepository;
// Query : permet d'ecrire soi-meme la requete.
import org.springframework.data.jpa.repository.Query;
// Param : relie un parametre Java (:recherche) a son nom dans la requete.
import org.springframework.data.repository.query.Param;

public interface PieceRepository extends JpaRepository<Piece, Long> {

    // ---- Recherche paginee (deja ecrite, inchangee) ----

    @Query("""
            SELECT p FROM Piece p
            WHERE (LOWER(p.reference) LIKE LOWER(CONCAT('%', :recherche, '%'))
                OR LOWER(p.designation) LIKE LOWER(CONCAT('%', :recherche, '%')))
              AND (:categorie IS NULL OR p.categorie = :categorie)
            """)
    Page<Piece> rechercher(@Param("recherche") String recherche,
                           @Param("categorie") CategoriePiece categorie,
                           Pageable pageable);

    // ---- Alertes de stock ----

    // Pieces dont la quantite en stock est inferieure ou egale au seuil d'alerte.
    // JPQL : on parle de la classe Piece et de ses champs, pas de la table SQL.
    // Aucun parametre venant de l'utilisateur : aucun risque d'injection SQL.
    // Tri : stock croissant (les plus urgentes d'abord), puis reference pour que
    // l'ordre soit toujours le meme quand deux pieces ont le meme stock.
    @Query("SELECT p FROM Piece p WHERE p.quantiteStock <= p.seuilAlerte "
            + "ORDER BY p.quantiteStock ASC, p.reference ASC")
    List<Piece> trouverEnAlerte();

    // ---- Questions "existe-t-il ?" (requetes fabriquees par le nom) ----

    // CREATION : cette reference est-elle deja prise ?
    boolean existsByReference(String reference);

    // MODIFICATION : prise par une AUTRE piece que celle qu'on modifie ?
    // IdNot = "dont l'id est different de celui-ci".
    boolean existsByReferenceAndIdNot(String reference, Long id);

    // ---- Statistiques (calculees EN BASE) ----

    // SUM sur un entier renvoie un Long. Table vide : null (le service met 0).
    @Query("SELECT SUM(p.quantiteStock) FROM Piece p")
    Long quantiteTotale();

    // Valeur du stock = somme de (prix unitaire x quantite). Table vide : null.
    @Query("SELECT SUM(p.prixUnitaire * p.quantiteStock) FROM Piece p")
    BigDecimal valeurTotale();

    // Nombre de pieces a reapprovisionner : quantite <= seuil d'alerte.
    @Query("SELECT COUNT(p) FROM Piece p WHERE p.quantiteStock <= p.seuilAlerte")
    long compterAlertes();

    // Par categorie : [categorie, nombre de pieces, valeur du stock de la categorie].
    @Query("SELECT p.categorie, COUNT(p), SUM(p.prixUnitaire * p.quantiteStock) FROM Piece p GROUP BY p.categorie")
    List<Object[]> statistiquesParCategorie();

    // La piece la plus chere : "findFirst" + "OrderByPrixUnitaireDesc" = la premiere apres tri decroissant.
    // Optional : une "boite" qui est vide si la table est vide.
    Optional<Piece> findFirstByOrderByPrixUnitaireDesc();

}