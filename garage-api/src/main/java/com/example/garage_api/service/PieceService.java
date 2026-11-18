package com.example.garage_api.service;

import com.example.garage_api.entity.Piece;
import com.example.garage_api.enums.CategoriePiece;
import com.example.garage_api.exception.ConflitException;
import com.example.garage_api.exception.IntrouvableException;
import com.example.garage_api.repository.LigneInterventionRepository;
import com.example.garage_api.repository.PieceRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PieceService {

    // Les deux magasiniers dont ce service a besoin.
    private final PieceRepository pieceRepository;
    private final LigneInterventionRepository ligneRepository;

    // Constructeur : Spring fournit lui-meme les deux repositories.
    public PieceService(PieceRepository pieceRepository,
                        LigneInterventionRepository ligneRepository) {
        this.pieceRepository = pieceRepository;
        this.ligneRepository = ligneRepository;
    }

    // LISTE paginee : recherche par reference ou designation + filtre de categorie facultatif.
    @Transactional(readOnly = true)
    public Page<Piece> lister(String recherche, CategoriePiece categorie, Pageable pageable) {
        // Sans texte, on cherche "" : cela correspond a toutes les pieces.
        String texte = (recherche == null) ? "" : recherche.trim();
        // categorie peut etre null : la requete saute alors le filtre (:categorie IS NULL).
        return pieceRepository.rechercher(texte, categorie, pageable);
    }

    // LIRE une piece par son id.
    @Transactional(readOnly = true)
    public Piece lire(Long id) {
        // Optional vide = piece absente : on lance l'exception "introuvable" (404 a l'etape 5).
        return pieceRepository.findById(id)
                .orElseThrow(() -> new IntrouvableException("Piece introuvable : " + id));
    }

    // LISTE des pieces en alerte (route /api/pieces/alertes).
    // readOnly = true : on lit seulement, on ne modifie rien.
    @Transactional(readOnly = true)
    public List<Piece> listerAlertes() {
        return pieceRepository.trouverEnAlerte();
    }

    // CREER une piece.
    @Transactional
    public Piece creer(Piece piece) {
        // Regle : la reference doit etre unique.
        if (pieceRepository.existsByReference(piece.getReference())) {
            throw new ConflitException("Cette reference existe deja : " + piece.getReference());
        }
        return pieceRepository.save(piece);
    }

    // MODIFIER une piece existante.
    @Transactional
    public Piece modifier(Long id, Piece donnees) {
        // 404 si la piece n'existe pas.
        Piece existante = lire(id);

        // Regle : la reference ne doit pas appartenir a UNE AUTRE piece.
        if (pieceRepository.existsByReferenceAndIdNot(donnees.getReference(), id)) {
            throw new ConflitException("Cette reference existe deja : " + donnees.getReference());
        }

        // On recopie champ par champ. Choix volontaire : on ne recopie PAS quantiteStock.
        // Le stock ne bouge que par les operations "entree" et "sortie" ci-dessous,
        // ce qui garde chaque mouvement sous controle.
        existante.setReference(donnees.getReference());
        existante.setDesignation(donnees.getDesignation());
        existante.setCategorie(donnees.getCategorie());
        existante.setFabricant(donnees.getFabricant());
        existante.setPrixUnitaire(donnees.getPrixUnitaire());
        existante.setSeuilAlerte(donnees.getSeuilAlerte());
        existante.setDateAchat(donnees.getDateAchat());
        return pieceRepository.save(existante);
    }

    // SUPPRIMER une piece.
    @Transactional
    public void supprimer(Long id) {
        // 404 si la piece n'existe pas.
        Piece piece = lire(id);

        // Regle : suppression refusee (409) si la piece figure sur au moins une ligne.
        if (ligneRepository.existsByPieceId(id)) {
            throw new ConflitException("Suppression impossible : cette piece est utilisee dans une intervention.");
        }
        pieceRepository.delete(piece);
    }

    // ENTREE de stock : on ajoute des pieces au stock (reapprovisionnement).
    @Transactional
    public Piece entrerStock(Long id, int quantite) {
        verifierQuantite(quantite);
        Piece piece = lire(id);
        piece.setQuantiteStock(piece.getQuantiteStock() + quantite);
        return pieceRepository.save(piece);
    }

    // SORTIE de stock : on retire des pieces du stock.
    // Cette methode sera aussi appelee par le service des interventions (etape E).
    @Transactional
    public Piece sortirStock(Long id, int quantite) {
        verifierQuantite(quantite);
        Piece piece = lire(id);

        // Regle : la quantite ne devient jamais negative.
        // Une sortie superieure au stock est refusee (409), et le stock reste inchange.
        if (quantite > piece.getQuantiteStock()) {
            throw new ConflitException("Stock insuffisant pour " + piece.getReference()
                    + " : " + piece.getQuantiteStock() + " en stock, " + quantite + " demande(s).");
        }
        piece.setQuantiteStock(piece.getQuantiteStock() - quantite);
        return pieceRepository.save(piece);
    }

    // Garde-fou : une entree ou une sortie d'au moins 1 piece.
    // IllegalArgumentException = "argument invalide" ; l'etape 5 la traduira en reponse 400.
    private void verifierQuantite(int quantite) {
        if (quantite < 1) {
            throw new IllegalArgumentException("La quantite doit etre superieure ou egale a 1.");
        }
    }
}