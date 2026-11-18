package com.example.garage_api.repository;

import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.StatutVehicule;

// Page : le resultat d'une recherche paginee (elements + nombre total).
import org.springframework.data.domain.Page;
// Pageable : la demande de pagination (numero de page, taille, tri).
import org.springframework.data.domain.Pageable;
// EntityGraph : dit a Hibernate quels liens charger en meme temps (anti N+1).
import org.springframework.data.jpa.repository.EntityGraph;
// JpaRepository : fournit deja save, findById, findAll, delete, count...
import org.springframework.data.jpa.repository.JpaRepository;
// Query : permet d'ecrire soi-meme la requete.
import org.springframework.data.jpa.repository.Query;
// Param : relie un parametre Java (:recherche) a son nom dans la requete.
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    // ---- Recherche paginee ----

    // Charge le client AVEC le vehicule, en une seule requete (anti N+1).
    @EntityGraph(attributePaths = "client")
    // Deux filtres facultatifs : le statut et le client.
    // Chacun suit le meme principe : ":x IS NULL OR champ = :x"
    // = "si aucun filtre n'est donne, on laisse tout passer ; sinon on compare".
    @Query("""
            SELECT v FROM Vehicule v
            WHERE (LOWER(v.marque) LIKE LOWER(CONCAT('%', :recherche, '%'))
                OR LOWER(v.modele) LIKE LOWER(CONCAT('%', :recherche, '%'))
                OR LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :recherche, '%')))
              AND (:statut IS NULL OR v.statut = :statut)
              AND (:clientId IS NULL OR v.client.id = :clientId)
            """)
    Page<Vehicule> rechercher(@Param("recherche") String recherche,
                              @Param("statut") StatutVehicule statut,
                              @Param("clientId") Long clientId,
                              Pageable pageable);

    // ---- Questions "existe-t-il ?" (requetes fabriquees par le nom) ----

    // CREATION : cette immatriculation est-elle deja prise ?
    boolean existsByImmatriculation(String immatriculation);

    // MODIFICATION : prise par un AUTRE vehicule que celui qu'on modifie ?
    boolean existsByImmatriculationAndIdNot(String immatriculation, Long id);

    // SUPPRESSION D'UN CLIENT : ce client possede-t-il au moins un vehicule ?
    boolean existsByClientId(Long clientId);

    // ---- Statistiques (calculees EN BASE, jamais en bouclant sur toute la table) ----

    // GROUP BY : regroupe les vehicules par statut et compte chaque groupe.
    // Chaque ligne du resultat est un tableau [statut, nombre].
    @Query("SELECT v.statut, COUNT(v) FROM Vehicule v GROUP BY v.statut")
    List<Object[]> compterParStatut();

    // Meme principe, regroupe par carburant.
    @Query("SELECT v.carburant, COUNT(v) FROM Vehicule v GROUP BY v.carburant")
    List<Object[]> compterParCarburant();

    // Les marques les plus nombreuses : ORDER BY ... DESC = la plus frequente d'abord.
    // Pageable sert ici a LIMITER le nombre de lignes (PageRequest.of(0, 5) = les 5 premieres).
    @Query("SELECT v.marque, COUNT(v) FROM Vehicule v GROUP BY v.marque ORDER BY COUNT(v) DESC, v.marque")
    List<Object[]> topMarques(Pageable pageable);

    // Moyenne du kilometrage. Table vide ou valeurs absentes : le resultat est null.
    // Le service remplacera ce null par 0.
    @Query("SELECT AVG(v.kilometrage) FROM Vehicule v")
    Double moyenneKilometrage();

    // Somme des couts estimes pour un statut donne. Aucun vehicule : null (le service met 0).
    @Query("SELECT SUM(v.coutEstime) FROM Vehicule v WHERE v.statut = :statut")
    BigDecimal sommeCoutEstime(@Param("statut") StatutVehicule statut);

    // Nombre de vehicules entres entre deux dates (inclus). Requete fabriquee par le nom.
    long countByDateEntreeBetween(LocalDate debut, LocalDate fin);
}