
package com.example.garage_api.service;

import com.example.garage_api.dto.PieceStatistiques;
import com.example.garage_api.dto.VehiculeStatistiques;
import com.example.garage_api.enums.CategoriePiece;
import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.repository.PieceRepository;
import com.example.garage_api.repository.VehiculeRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatistiquesServiceTest {

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private PieceRepository pieceRepository;

    @InjectMocks
    private StatistiquesService statistiquesService;

    @Test
    void statistiquesVehicules_BaseVide_RetourneDesZeros() {

        when(vehiculeRepository.compterParStatut())
                .thenReturn(List.of());

        when(vehiculeRepository.compterParCarburant())
                .thenReturn(List.of());

        when(vehiculeRepository.topMarques(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        when(vehiculeRepository.moyenneKilometrage())
                .thenReturn(null);

        when(vehiculeRepository.sommeCoutEstime(StatutVehicule.EN_REPARATION))
                .thenReturn(null);

        when(vehiculeRepository.countByDateEntreeBetween(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(0L);

        VehiculeStatistiques resultat =
                statistiquesService.statistiquesVehicules();

        assertEquals(0L, resultat.total());
        assertEquals(0.0, resultat.kilometrageMoyen());
        assertEquals(BigDecimal.ZERO, resultat.coutEstimeEnReparation());
        assertEquals(0L, resultat.entresCeMois());

        assertTrue(resultat.topMarques().isEmpty());

        assertEquals(StatutVehicule.values().length,
                resultat.parStatut().size());

        assertTrue(resultat.parStatut().values()
                .stream().allMatch(nombre -> nombre == 0L));

        assertEquals(Carburant.values().length,
                resultat.parCarburant().size());

        assertTrue(resultat.parCarburant().values()
                .stream().allMatch(nombre -> nombre == 0L));
    }

    @Test
    void statistiquesPieces_BaseVide_RetourneDesZeros() {

        when(pieceRepository.quantiteTotale())
                .thenReturn(null);

        when(pieceRepository.valeurTotale())
                .thenReturn(null);

        when(pieceRepository.compterAlertes())
                .thenReturn(0L);

        when(pieceRepository.statistiquesParCategorie())
                .thenReturn(List.of());

        when(pieceRepository.findFirstByOrderByPrixUnitaireDesc())
                .thenReturn(Optional.empty());

        PieceStatistiques resultat =
                statistiquesService.statistiquesPieces();

        assertEquals(0L, resultat.nombreReferences());
        assertEquals(0L, resultat.quantiteTotale());
        assertEquals(BigDecimal.ZERO, resultat.valeurStock());
        assertEquals(0L, resultat.enAlerte());

        assertNull(resultat.pieceLaPlusChere());

        assertEquals(CategoriePiece.values().length,
                resultat.parCategorie().size());

        assertTrue(resultat.parCategorie().stream()
                .allMatch(categorie ->
                        categorie.nombre() == 0L
                                && categorie.valeur().compareTo(BigDecimal.ZERO) == 0));
    }
}
