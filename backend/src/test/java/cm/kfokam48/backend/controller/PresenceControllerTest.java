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
 * B6 — contrat de POST /api/presences (issues #2, EF2/RG2). Le scénario de test
 * hérite de la base SessionScenario (créée une fois par classe de test via SQL).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private String code;
    private long etudiantId;

    @BeforeEach
    void preparerScenario() {
        jdbc.update("insert into promotion (nom) values (?)", "Licence 2");
        long promo = jdbc.queryForObject("select max(id) from promotion", Long.class);
        jdbc.update("insert into etudiant (promotion_id, nom) values (?, ?)", promo, "Nadège");
        etudiantId = jdbc.queryForObject("select max(id) from etudiant", Long.class);

        // session récemment ouverte : code valide, non expiré
        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                + "values (?, ?, ?, current_timestamp, dateadd('MINUTE', 10, current_timestamp))", promo, "Algèbre", "AAA111");
        code = "AAA111";
    }

    @Test
    void unePresenceValideEstEnregistree() throws Exception {
        mvc.perform(post("/api/presences").contentType("application/json")
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.etudiantId").value(etudiantId))
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    void etudiantInconnuRenvoie404() throws Exception {
        mvc.perform(post("/api/presences").contentType("application/json")
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }
}
