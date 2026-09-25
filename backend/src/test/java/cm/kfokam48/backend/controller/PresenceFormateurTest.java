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
 * Issue #6 — EF4/RG12 (Q14) : le formateur ajoute une présence à la main, elle
 * est marquée source=FORMATEUR (« ajouté par le formateur »). H5 : refus après
 * clôture. RG2 : l'unicité vaut quelle que soit la source.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceFormateurTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long sessionId;
    private long etudiantId;

    @BeforeEach
    void preparerScenario() {
        jdbc.update("insert into promotion (nom) values (?)", "Licence 2");
        long promo = jdbc.queryForObject("select max(id) from promotion", Long.class);
        jdbc.update("insert into etudiant (promotion_id, nom) values (?, ?)", promo, "Nadège");
        etudiantId = jdbc.queryForObject("select max(id) from etudiant", Long.class);

        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                + "values (?, ?, ?, current_timestamp, dateadd('MINUTE', 10, current_timestamp))", promo, "Algèbre", "FRM444");
        sessionId = jdbc.queryForObject("select max(id) from session_cours", Long.class);
    }

    @Test
    void leFormateurAjouteUnePresenceMarqueeFormateur() throws Exception {
        mvc.perform(post("/api/sessions/" + sessionId + "/presences")
                        .contentType("application/json")
                        .content("{\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.etudiantId").value(etudiantId))
                .andExpect(jsonPath("$.source").value("FORMATEUR"));
    }

    @Test
    void sessionInconnueRenvoie404() throws Exception {
        mvc.perform(post("/api/sessions/999999/presences")
                        .contentType("application/json")
                        .content("{\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }

    @Test
    void sessionClotureeRenvoie409() throws Exception {
        jdbc.update("update session_cours set cloture_at = dateadd('MINUTE', -1, current_timestamp) where id = ?", sessionId);

        mvc.perform(post("/api/sessions/" + sessionId + "/presences")
                        .contentType("application/json")
                        .content("{\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void uniciteVautQuelqueSoitLaSource() throws Exception {
        // l'étudiant s'est déjà marqué lui-même (source=ETUDIANT) : le formateur ne peut pas doubler
        jdbc.update("insert into presence (session_id, etudiant_id, source, cree_at) "
                + "values (?, ?, 'ETUDIANT', current_timestamp)", sessionId, etudiantId);

        mvc.perform(post("/api/sessions/" + sessionId + "/presences")
                        .contentType("application/json")
                        .content("{\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }
}
