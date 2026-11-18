package com.example.garage_api.repository;

import com.example.garage_api.entity.LigneIntervention;

// JpaRepository : fournit deja save, findById, findAll, delete, count...
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// "Un magasinier des LigneIntervention dont l'id est un Long".
public interface LigneInterventionRepository extends JpaRepository<LigneIntervention, Long> {

    // SUPPRESSION D'UNE PIECE : cette piece figure-t-elle sur au moins une ligne ?
    //   exists  = "y a-t-il au moins un ..." (renvoie true ou false)
    //   Piece   = le champ "piece" de LigneIntervention (le lien vers la piece)
    //   Id      = le champ "id" de cette piece
    // Spring suit le chemin ligne.piece.id tout seul, comme pour existsByClientId.
    boolean existsByPieceId(Long pieceId);

    // ---- Statistiques (calculees EN BASE) ----

    // Montant de toutes les pieces facturees (prix copie x quantite). Aucune ligne : null.
    @Query("SELECT SUM(l.prixUnitaire * l.quantite) FROM LigneIntervention l")
    BigDecimal sommeToutesLignes();

    // Meme somme pour les interventions commencees entre deux dates (inclus).
    @Query("SELECT SUM(l.prixUnitaire * l.quantite) FROM LigneIntervention l "
            + "WHERE l.intervention.dateDebut BETWEEN :debut AND :fin")
    BigDecimal sommeLignesEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    // Pieces facturees par chaque client : [id, prenom, nom, total].
    @Query("SELECT c.id, c.prenom, c.nom, SUM(l.prixUnitaire * l.quantite) FROM LigneIntervention l "
            + "JOIN l.intervention i JOIN i.vehicule v JOIN v.client c GROUP BY c.id, c.prenom, c.nom")
    List<Object[]> depensesPiecesParClient();

    // Les pieces les plus utilisees : [reference, designation, quantite totale], la plus utilisee d'abord.
    // Pageable limite le nombre de lignes (PageRequest.of(0, 5) = les 5 premieres).
    @Query("SELECT p.reference, p.designation, SUM(l.quantite) FROM LigneIntervention l JOIN l.piece p "
            + "GROUP BY p.id, p.reference, p.designation ORDER BY SUM(l.quantite) DESC, p.reference")
    List<Object[]> piecesLesPlusUtilisees(Pageable pageable);

}