
package com.example.garage_api.controller;

import com.example.garage_api.entity.Client;
import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.exception.ConflitException;
import com.example.garage_api.exception.GestionnaireErreurs;
import com.example.garage_api.exception.IntrouvableException;
import com.example.garage_api.service.VehiculeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculeController.class)
@Import(GestionnaireErreurs.class)
class VehiculeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehiculeService vehiculeService;

    // Données JSON valides pour les tests POST et PUT.
    private String jsonValide() {
        return """
            {
                "clientId": 1,
                "immatriculation": "AB-123-CD",
                "marque": "Toyota",
                "modele": "Corolla",
                "annee": 2020,
                "kilometrage": 50000,
                "carburant": "ESSENCE",
                "statut": "EN_ATTENTE",
                "dateEntree": "2025-01-10",
                "coutEstime": 125000,
                "panne": "Bruit moteur"
            }
            """;
    }

    // Fabrique un véhicule avec son client pour la conversion en VehiculeReponse.
    private Vehicule vehiculeExemple() {
        Client client = new Client();
        client.setNom("Dupont");
        client.setPrenom("Jean");

        Vehicule v = new Vehicule();
        v.setImmatriculation("AB-123-CD");
        v.setMarque("Toyota");
        v.setModele("Corolla");
        v.setAnnee(2020);
        v.setKilometrage(50000);
        v.setCarburant(Carburant.ESSENCE);
        v.setStatut(StatutVehicule.EN_ATTENTE);
        v.setDateEntree(LocalDate.of(2025, 1, 10));
        v.setCoutEstime(new BigDecimal("125000"));
        v.setPanne("Bruit moteur");
        v.setClient(client);

        return v;
    }

    // TEST 1 : GET /api/vehicules répond 200.
    @Test
    void lister_retourne200() throws Exception {
        when(vehiculeService.lister(
                nullable(String.class),
                nullable(StatutVehicule.class),
                nullable(Long.class),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/vehicules"))
                .andExpect(status().isOk());
    }

    // TEST 2 : GET /api/vehicules/{id} répond 404 si absent.
    @Test
    void lire_vehiculeAbsent_retourne404() throws Exception {
        when(vehiculeService.lire(9999L))
                .thenThrow(new IntrouvableException("Vehicule introuvable : 9999"));

        mockMvc.perform(get("/api/vehicules/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Vehicule introuvable : 9999"));
    }

    // TEST 3 : POST /api/vehicules répond 201 si les données sont valides.
    @Test
    void creer_donneesValides_retourne201() throws Exception {
        when(vehiculeService.creer(any(Vehicule.class), eq(1L)))
                .thenReturn(vehiculeExemple());

        mockMvc.perform(post("/api/vehicules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValide()))
                .andExpect(status().isCreated());
    }

    // TEST 4 : POST avec une immatriculation invalide répond 400.
    @Test
    void creer_immatriculationInvalide_retourne400() throws Exception {
        String json = jsonValide().replace("AB-123-CD", "AB123CD");

        mockMvc.perform(post("/api/vehicules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Données invalides"))
                .andExpect(jsonPath("$.erreurs.immatriculation").exists());
    }

    // TEST 5 : POST avec une immatriculation déjà utilisée répond 409.
    @Test
    void creer_doublon_retourne409() throws Exception {
        when(vehiculeService.creer(any(Vehicule.class), eq(1L)))
                .thenThrow(new ConflitException("Cette immatriculation existe déjà"));

        mockMvc.perform(post("/api/vehicules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValide()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    // TEST 6 : PUT /api/vehicules/{id} répond 200.
    @Test
    void modifier_donneesValides_retourne200() throws Exception {
        when(vehiculeService.modifier(
                eq(1L), any(Vehicule.class), eq(1L)
        )).thenReturn(vehiculeExemple());

        mockMvc.perform(put("/api/vehicules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValide()))
                .andExpect(status().isOk());
    }

    // TEST 7 : PATCH avec une transition interdite répond 409.
    @Test
    void changerStatut_transitionInterdite_retourne409() throws Exception {
        when(vehiculeService.changerStatut(1L, StatutVehicule.TERMINE))
                .thenThrow(new ConflitException("Transition de statut interdite"));

        mockMvc.perform(patch("/api/vehicules/1/statut")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"statut": "TERMINE"}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    // TEST 8 : DELETE /api/vehicules/{id} répond 204.
    @Test
    void supprimer_retourne204() throws Exception {
        mockMvc.perform(delete("/api/vehicules/1"))
                .andExpect(status().isNoContent());
    }

    // TEST 9 : GET /api/clients/{id}/vehicules répond 200.
    @Test
    void listerParClient_retourne200() throws Exception {
        when(vehiculeService.listerParClient(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/clients/1/vehicules"))
                .andExpect(status().isOk());
    }
}
