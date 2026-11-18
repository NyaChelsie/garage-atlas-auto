package com.example.garage_api.service;

import com.example.garage_api.dto.PieceStatistiques;
import com.example.garage_api.dto.VehiculeStatistiques;
import com.example.garage_api.enums.CategoriePiece;
import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.repository.PieceRepository;
import com.example.garage_api.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatistiquesService {

    private final VehiculeRepository vehiculeRepository;
    private final PieceRepository pieceRepository;

    public StatistiquesService(VehiculeRepository vehiculeRepository,
                               PieceRepository pieceRepository) {
        this.vehiculeRepository = vehiculeRepository;
        this.pieceRepository = pieceRepository;
    }

    // STATISTIQUES DES VEHICULES.
    @Transactional(readOnly = true)
    public VehiculeStatistiques statistiquesVehicules() {
        long total = vehiculeRepository.count();

        // On part d'une table ou CHAQUE statut vaut 0, puis on remplit avec les vrais chiffres.
        Map<StatutVehicule, Long> parStatut = new LinkedHashMap<>();
        for (StatutVehicule statut : StatutVehicule.values()) {
            parStatut.put(statut, 0L);
        }
        for (Object[] ligne : vehiculeRepository.compterParStatut()) {
            parStatut.put((StatutVehicule) ligne[0], (Long) ligne[1]);
        }

        Map<Carburant, Long> parCarburant = new LinkedHashMap<>();
        for (Carburant carburant : Carburant.values()) {
            parCarburant.put(carburant, 0L);
        }
        for (Object[] ligne : vehiculeRepository.compterParCarburant()) {
            parCarburant.put((Carburant) ligne[0], (Long) ligne[1]);
        }

        // Top 5 des marques : PageRequest.of(0, 5) limite le resultat a 5 lignes.
        List<VehiculeStatistiques.MarqueCompte> topMarques = vehiculeRepository
                .topMarques(PageRequest.of(0, 5))
                .stream()
                .map(ligne -> new VehiculeStatistiques.MarqueCompte((String) ligne[0], (Long) ligne[1]))
                .toList();

        // La moyenne d'une table vide vaut null : on la remplace par 0.
        Double moyenne = vehiculeRepository.moyenneKilometrage();
        double kilometrageMoyen = (moyenne == null) ? 0 : Math.round(moyenne * 10) / 10.0;

        BigDecimal cout = vehiculeRepository.sommeCoutEstime(StatutVehicule.EN_REPARATION);
        if (cout == null) {
            cout = BigDecimal.ZERO;
        }

        // "Ce mois-ci" : du 1er au dernier jour du mois en cours.
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        long entresCeMois = vehiculeRepository.countByDateEntreeBetween(debut, fin);

        return new VehiculeStatistiques(total, parStatut, parCarburant, topMarques,
                kilometrageMoyen, cout, entresCeMois);
    }

    // STATISTIQUES DES PIECES.
    @Transactional(readOnly = true)
    public PieceStatistiques statistiquesPieces() {
        long references = pieceRepository.count();

        // SUM d'une table vide = null : on remplace par 0.
        Long quantite = pieceRepository.quantiteTotale();
        long quantiteTotale = (quantite == null) ? 0 : quantite;
        BigDecimal valeur = pieceRepository.valeurTotale();
        if (valeur == null) {
            valeur = BigDecimal.ZERO;
        }

        long enAlerte = pieceRepository.compterAlertes();

        // Chaque categorie existe dans le resultat, meme sans piece (nombre 0, valeur 0).
        Map<CategoriePiece, PieceStatistiques.CategorieStat> categories = new LinkedHashMap<>();
        for (CategoriePiece categorie : CategoriePiece.values()) {
            categories.put(categorie, new PieceStatistiques.CategorieStat(categorie, 0L, BigDecimal.ZERO));
        }
        // Chaque ligne est un tableau [categorie, nombre, valeur].
        for (Object[] ligne : pieceRepository.statistiquesParCategorie()) {
            CategoriePiece categorie = (CategoriePiece) ligne[0];
            BigDecimal valeurCategorie = (ligne[2] == null) ? BigDecimal.ZERO : (BigDecimal) ligne[2];
            categories.put(categorie,
                    new PieceStatistiques.CategorieStat(categorie, (Long) ligne[1], valeurCategorie));
        }

        // map : transforme la piece (si elle existe) ; orElse(null) : sinon, pas de piece.
        PieceStatistiques.PieceChere pieceChere = pieceRepository.findFirstByOrderByPrixUnitaireDesc()
                .map(p -> new PieceStatistiques.PieceChere(p.getReference(), p.getDesignation(), p.getPrixUnitaire()))
                .orElse(null);

        return new PieceStatistiques(references, quantiteTotale, valeur, enAlerte,
                List.copyOf(categories.values()), pieceChere);
    }
}