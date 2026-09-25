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
 * Issue #4 — RG1 (Q2) : le code ne marche plus après expiration (410) et un
 * code inexistant est refusé (400), au format d'erreur imposé. La session
 * clôturée renvoie aussi 410 : une session clôturée a nécessairement son code
 * expiré (voir D3).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceCodeErreursTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long etudiantId;

    @BeforeEach
    void preparerScenario() {
        jdbc.update("insert into promotion (nom) values (?)", "Licence 2");
        long promo = jdbc.queryForObject("select max(id) from promotion", Long.class);
        jdbc.update("insert into etudiant (promotion_id, nom) values (?, ?)", promo, "Nadège");
        etudiantId = jdbc.queryForObject("select max(id) from etudiant", Long.class);

        // session ouverte il y a 20 min, expirée il y a 5 min (RG1)
        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                        + "values (?, ?, ?, dateadd('MINUTE', -20, current_timestamp), dateadd('MINUTE', -5, current_timestamp))",
                promo, "Algèbre", "EXP123");

        // session clôturée il y a 1 min (le code est de toute façon expiré)
        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at, cloture_at) "
                        + "values (?, ?, ?, dateadd('MINUTE', -30, current_timestamp), dateadd('MINUTE', -15, current_timestamp), "
                        + "dateadd('MINUTE', -1, current_timestamp))",
                promo, "Réseaux", "CLO555");
    }

    @Test
    void codeInconnuRenvoie400() throws Exception {
        mvc.perform(post("/api/presences").contentType("application/json")
                        .content("{\"code\":\"ZZZ999\",\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void codeExpireRenvoie410() throws Exception {
        mvc.perform(post("/api/presences").contentType("application/json")
                        .content("{\"code\":\"EXP123\",\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void sessionClotureeRenvoie410() throws Exception {
        mvc.perform(post("/api/presences").contentType("application/json")
                        .content("{\"code\":\"CLO555\",\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }
}
