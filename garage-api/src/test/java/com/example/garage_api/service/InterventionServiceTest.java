package com.example.garage_api.service;

import com.example.garage_api.entity.Intervention;
import com.example.garage_api.entity.LigneIntervention;
import com.example.garage_api.entity.Piece;
import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.exception.ConflitException;
import com.example.garage_api.repository.InterventionRepository;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Meme principe que pour les vehicules : pas de base de donnees, des repositories
// et des services "faux" qui repondent ce qu'on leur dicte.
@ExtendWith(MockitoExtension.class)
class InterventionServiceTest {

    @Mock
    private InterventionRepository interventionRepository;
    @Mock
    private VehiculeService vehiculeService;
    @Mock
    private PieceService pieceService;

    // Le VRAI InterventionService, construit avec les trois faux ci-dessus.
    @InjectMocks
    private InterventionService interventionService;

    // TEST 7 : ajouter une ligne avec un stock insuffisant = 409.
    // Aucune ligne n'est creee et rien n'est enregistre (scenario 28).
    @Test
    void ajouterLigne_stockInsuffisant_nEnregistreRien() {
        Intervention intervention = new Intervention();
        when(interventionRepository.findById(1L)).thenReturn(Optional.of(intervention));
        // sortirStock est l'etape qui verifie ET diminue le stock : on la fait echouer.
        when(pieceService.sortirStock(3L, 50))
                .thenThrow(new ConflitException("Stock insuffisant"));

        assertThrows(ConflitException.class, () -> interventionService.ajouterLigne(1L, 3L, 50));

        // L'etape 2 (creer la ligne) n'a jamais eu lieu : la liste reste vide, save jamais appele.
        assertTrue(intervention.getLignes().isEmpty());
        verify(interventionRepository, never()).save(any());
    }

    // TEST 8 : la meme piece ne peut figurer qu'une fois par intervention (409).
    // Le stock n'est meme pas touche : le refus arrive AVANT la sortie de stock.
    @Test
    void ajouterLigne_refuseUneMemePieceDeuxFois() {
        // Une piece "factice" dont on impose l'id : on n'a pas besoin d'une vraie piece ici.
        Piece piece = mock(Piece.class);
        when(piece.getId()).thenReturn(3L);

        LigneIntervention existante = new LigneIntervention();
        existante.setPiece(piece);
        Intervention intervention = new Intervention();
        intervention.ajouterLigne(existante);   // la piece 3 est deja sur l'intervention
        when(interventionRepository.findById(1L)).thenReturn(Optional.of(intervention));

        assertThrows(ConflitException.class, () -> interventionService.ajouterLigne(1L, 3L, 1));

        // anyLong() / anyInt() : "quels que soient les arguments".
        verify(pieceService, never()).sortirStock(anyLong(), anyInt());
    }

    // TEST 9 : une intervention est refusee (409) si le vehicule est deja LIVRE.
    @Test
    void creer_refuseUnVehiculeLivre() {
        Vehicule livre = new Vehicule();
        livre.setStatut(StatutVehicule.LIVRE);
        when(vehiculeService.lire(1L)).thenReturn(livre);

        assertThrows(ConflitException.class,
                () -> interventionService.creer(new Intervention(), 1L));
        verify(interventionRepository, never()).save(any());
    }

    // TEST 10 : un ajout reussi copie le prix de la piece et recalcule le total.
    // Le total = main-d'oeuvre (absente = 0) + quantite x prix = 2 x 15 000 = 30 000.
    @Test
    void ajouterLigne_copieLePrixEtCalculeLeTotal() {
        Intervention intervention = new Intervention();
        when(interventionRepository.findById(1L)).thenReturn(Optional.of(intervention));

        Piece piece = new Piece();
        piece.setPrixUnitaire(new BigDecimal("15000"));
        when(pieceService.sortirStock(3L, 2)).thenReturn(piece);
        when(interventionRepository.save(intervention)).thenReturn(intervention);

        Intervention resultat = interventionService.ajouterLigne(1L, 3L, 2);

        assertEquals(1, resultat.getLignes().size());
        LigneIntervention ligne = resultat.getLignes().get(0);
        assertEquals(2, ligne.getQuantite());
        // compareTo == 0 : egalite de VALEUR pour les BigDecimal (30000 et 30000.00 sont egaux).
        assertEquals(0, new BigDecimal("15000").compareTo(ligne.getPrixUnitaire()));
        assertEquals(0, new BigDecimal("30000").compareTo(resultat.getTotal()));
    }
}