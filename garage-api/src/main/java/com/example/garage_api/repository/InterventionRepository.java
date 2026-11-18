package com.example.garage_api.repository;

import com.example.garage_api.entity.Intervention;

// List : une liste ordonnee d'elements.
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// EntityGraph : dit a Hibernate quels liens charger en meme temps (anti N+1).
import org.springframework.data.jpa.repository.EntityGraph;
// JpaRepository : fournit deja save, findById, findAll, delete, count...
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// "Un magasinier des Intervention dont l'id est un Long".
public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    // SUPPRESSION D'UN VEHICULE : ce vehicule a-t-il au moins une intervention ?
    boolean existsByVehiculeId(Long vehiculeId);

    // HISTORIQUE D'UN VEHICULE, de la plus recente a la plus ancienne.
    // @EntityGraph : on charge les lignes ET la piece de chaque ligne dans la MEME requete.
    //   "lignes"       = la liste des lignes de l'intervention
    //   "lignes.piece" = la piece de chacune de ces lignes
    // Sans cela : 1 requete pour les interventions + 1 par intervention + 1 par ligne (N+1).
    @EntityGraph(attributePaths = {"lignes", "lignes.piece"})
    List<Intervention> findByVehiculeIdOrderByDateDebutDesc(Long vehiculeId);
    // ---- Statistiques (calculees EN BASE) ----

    // Somme de toute la main-d'oeuvre. Table vide ou valeurs absentes : null (le service met 0).
    @Query("SELECT SUM(i.mainOeuvre) FROM Intervention i")
    BigDecimal sommeMainOeuvre();

    // Meme somme, pour les interventions commencees entre deux dates (inclus).
    @Query("SELECT SUM(i.mainOeuvre) FROM Intervention i WHERE i.dateDebut BETWEEN :debut AND :fin")
    BigDecimal sommeMainOeuvreEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    // Main-d'oeuvre depensee par chaque client : [id, prenom, nom, total].
    // JOIN : on suit les liens intervention -> vehicule -> client.
    @Query("SELECT c.id, c.prenom, c.nom, SUM(i.mainOeuvre) FROM Intervention i "
            + "JOIN i.vehicule v JOIN v.client c GROUP BY c.id, c.prenom, c.nom")
    List<Object[]> depensesMainOeuvreParClient();

    // Vehicules qui n'ont AUCUNE intervention. NOT EXISTS : "il n'existe pas d'intervention pour lui".
    @Query("SELECT COUNT(v) FROM Vehicule v WHERE NOT EXISTS "
            + "(SELECT i.id FROM Intervention i WHERE i.vehicule.id = v.id)")
    long compterVehiculesSansIntervention();

}