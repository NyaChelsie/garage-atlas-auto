package com.example.garage_api.repository;

import com.example.garage_api.entity.Client;

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

// "Un magasinier des Client dont l'id est un Long".
public interface ClientRepository extends JpaRepository<Client, Long> {

    // ---- Recherche paginee (nouvelle) ----

    // Cherche dans le nom, le prenom OU le telephone, sans tenir compte de la casse.
    // LOWER(...) des deux cotes + CONCAT('%', :recherche, '%') = "contient".
    // Le telephone est un texte (9 chiffres) : LIKE fonctionne aussi dessus.
    // Le texte tape est un PARAMETRE (:recherche), jamais colle dans la requete :
    // c'est la protection contre l'injection SQL exigee par le sujet.
    @Query("""
            SELECT c FROM Client c
            WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :recherche, '%'))
               OR LOWER(c.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))
               OR c.telephone LIKE CONCAT('%', :recherche, '%')
            """)
    Page<Client> rechercher(@Param("recherche") String recherche, Pageable pageable);

    // ---- Questions "existe-t-il ?" (deja ecrites, inchangees) ----

    // CREATION : ce telephone est-il deja pris ?
    boolean existsByTelephone(String telephone);

    // MODIFICATION : pris par un AUTRE client que celui qu'on modifie ?
    boolean existsByTelephoneAndIdNot(String telephone, Long id);
}