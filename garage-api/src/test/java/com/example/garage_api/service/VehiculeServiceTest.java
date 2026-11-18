package com.example.garage_api.service;

import com.example.garage_api.entity.Client;
import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.exception.ConflitException;
import com.example.garage_api.exception.IntrouvableException;
import com.example.garage_api.repository.InterventionRepository;
import com.example.garage_api.repository.VehiculeRepository;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// MockitoExtension : active Mockito. On teste la LOGIQUE du service, sans base de donnees :
// les repositories sont remplaces par des "faux" qui repondent ce qu'on leur dicte.
@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

    // @Mock : un faux objet. Il ne parle pas a la base, il repond selon nos when(...).
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private InterventionRepository interventionRepository;
    @Mock
    private ClientService clientService;

    // @InjectMocks : cree le VRAI VehiculeService en lui donnant les trois faux ci-dessus.
    @InjectMocks
    private VehiculeService vehiculeService;

    // Petit outil : fabrique un vehicule a un statut donne, pour ne pas repeter le code.
    private Vehicule vehiculeAuStatut(StatutVehicule statut) {
        Vehicule v = new Vehicule();
        v.setImmatriculation("AB-123-CD");
        v.setStatut(statut);
        return v;
    }

    // TEST 1 : une immatriculation deja prise est refusee (409), rien n'est enregistre.
    @Test
    void creer_refuseUneImmatriculationDejaExistante() {
        Vehicule v = vehiculeAuStatut(StatutVehicule.EN_ATTENTE);
        when(clientService.lire(1L)).thenReturn(new Client());
        when(vehiculeRepository.existsByImmatriculation("AB-123-CD")).thenReturn(true);

        // assertThrows : le test reussit SEULEMENT si cette exception est lancee.
        assertThrows(ConflitException.class, () -> vehiculeService.creer(v, 1L));
        // never() : on verifie que save n'a jamais ete appele.
        verify(vehiculeRepository, never()).save(any());
    }

    // TEST 2 : creer avec un client inexistant = 404, aucun vehicule cree (scenario 23).
    @Test
    void creer_refuseUnClientInexistant() {
        Vehicule v = vehiculeAuStatut(StatutVehicule.EN_ATTENTE);
        when(clientService.lire(99L)).thenThrow(new IntrouvableException("Client introuvable : 99"));

        assertThrows(IntrouvableException.class, () -> vehiculeService.creer(v, 99L));
        verify(vehiculeRepository, never()).save(any());
    }

    // TEST 3 : EN_ATTENTE -> TERMINE est une transition interdite (409).
    @Test
    void changerStatut_refuseUneTransitionInterdite() {
        Vehicule v = vehiculeAuStatut(StatutVehicule.EN_ATTENTE);
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));

        assertThrows(ConflitException.class,
                () -> vehiculeService.changerStatut(1L, StatutVehicule.TERMINE));
        // Le statut n'a pas bouge et rien n'a ete enregistre.
        assertEquals(StatutVehicule.EN_ATTENTE, v.getStatut());
        verify(vehiculeRepository, never()).save(any());
    }

    // TEST 4 : EN_ATTENTE -> EN_REPARATION est permise : le statut change.
    @Test
    void changerStatut_accepteUneTransitionPermise() {
        Vehicule v = vehiculeAuStatut(StatutVehicule.EN_ATTENTE);
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));
        when(vehiculeRepository.save(v)).thenReturn(v);

        Vehicule resultat = vehiculeService.changerStatut(1L, StatutVehicule.EN_REPARATION);

        assertEquals(StatutVehicule.EN_REPARATION, resultat.getStatut());
    }

    // TEST 5 : un vehicule qui a des interventions ne peut pas etre supprime (409).
    @Test
    void supprimer_refuseUnVehiculeAvecDesInterventions() {
        Vehicule v = vehiculeAuStatut(StatutVehicule.TERMINE);
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));
        when(interventionRepository.existsByVehiculeId(1L)).thenReturn(true);

        assertThrows(ConflitException.class, () -> vehiculeService.supprimer(1L));
        verify(vehiculeRepository, never()).delete(any());
    }

    // TEST 6 : lire un identifiant inexistant = 404, avec l'id dans le message.
    @Test
    void lire_refuseUnIdentifiantInexistant() {
        when(vehiculeRepository.findById(9999L)).thenReturn(Optional.empty());

        IntrouvableException erreur =
                assertThrows(IntrouvableException.class, () -> vehiculeService.lire(9999L));
        assertTrue(erreur.getMessage().contains("9999"));
    }
}