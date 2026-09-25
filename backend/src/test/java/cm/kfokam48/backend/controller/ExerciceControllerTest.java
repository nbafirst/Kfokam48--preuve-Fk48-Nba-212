package cm.kfokam48.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #7 — EF5/RG9 (Q12) et la distinction C2 (section 7) :
 * le dépôt reste possible après l'expiration du code de présence (RG1 ne
 * concerne que la présence) mais pas après la clôture de la session.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExerciceControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long promo;
    private long etudiantId;
    private long sessionId;
    private long sessionExpiréeId;

    @BeforeEach
    void preparerScenario() {
        jdbc.update("insert into promotion (nom) values (?)", "Licence 2");
        promo = jdbc.queryForObject("select max(id) from promotion", Long.class);
        jdbc.update("insert into etudiant (promotion_id, nom) values (?, ?)", promo, "Nadège");
        etudiantId = jdbc.queryForObject("select max(id) from etudiant", Long.class);

        // session ouverte, code non expiré
        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                + "values (?, ?, ?, current_timestamp, dateadd('MINUTE', 10, current_timestamp))", promo, "Algèbre", "EXO555");
        sessionId = jdbc.queryForObject("select max(id) from session_cours", Long.class);

        // session dont le code a expiré mais qui n'est PAS clôturée (RG9/Q12)
        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                        + "values (?, ?, ?, dateadd('MINUTE', -30, current_timestamp), dateadd('MINUTE', -15, current_timestamp))",
                promo, "Réseaux", "EXP777");
        sessionExpiréeId = jdbc.queryForObject("select max(id) from session_cours", Long.class);
    }

    private String corps(long sid) {
        return "{\"sessionId\":" + sid + ",\"etudiantId\":" + etudiantId
                + ",\"lien\":\"https://github.com/etudiant/exercice-1\"}";
    }

    @Test
    void leDepotCreeUnExerciceEnAttente() throws Exception {
        mvc.perform(post("/api/exercices").contentType("application/json").content(corps(sessionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));
    }

    @Test
    void leDepotRestePossibleApresExpirationDuCode() throws Exception {
        // RG9/Q12 : le code est expiré depuis 15 min, la session n'est pas clôturée
        mvc.perform(post("/api/exercices").contentType("application/json").content(corps(sessionExpiréeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));
    }

    @Test
    void sessionClotureeRefuseLeDepot() throws Exception {
        jdbc.update("update session_cours set cloture_at = dateadd('MINUTE', -1, current_timestamp) where id = ?", sessionId);

        mvc.perform(post("/api/exercices").contentType("application/json").content(corps(sessionId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void unSecondDepotRenvoie409() throws Exception {
        mvc.perform(post("/api/exercices").contentType("application/json").content(corps(sessionId)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/exercices").contentType("application/json").content(corps(sessionId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
    }

    @Test
    void lienInvalideRenvoie400() throws Exception {
        mvc.perform(post("/api/exercices").contentType("application/json")
                        .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + etudiantId
                                + ",\"lien\":\"pas une adresse\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    void sessionInconnueRenvoie404() throws Exception {
        mvc.perform(post("/api/exercices").contentType("application/json")
                        .content("{\"sessionId\":999999,\"etudiantId\":" + etudiantId
                                + ",\"lien\":\"https://exemple.com/x\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }
}
