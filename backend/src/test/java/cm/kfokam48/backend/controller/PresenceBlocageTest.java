package cm.kfokam48.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #5 — RG3 (Q4) : 5 codes erronés → blocage 2 minutes, même avec un code
 * valide ensuite ; déblocage après la fenêtre. Les tentatives sont pré-chargées
 * avec des horodatages proches pour tester la fenêtre glissante sans attendre.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceBlocageTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long etudiantId;
    private long promo;

    @BeforeEach
    void preparerScenario() {
        jdbc.update("insert into promotion (nom) values (?)", "Licence 2");
        promo = jdbc.queryForObject("select max(id) from promotion", Long.class);
        jdbc.update("insert into etudiant (promotion_id, nom) values (?, ?)", promo, "Nadège");
        etudiantId = jdbc.queryForObject("select max(id) from etudiant", Long.class);

        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                + "values (?, ?, ?, current_timestamp, dateadd('MINUTE', 10, current_timestamp))", promo, "Algèbre", "BLK333");
    }

    private String corps(String code) {
        return "{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}";
    }

    @Test
    void cinqErreursDeclenchentLeBlocage() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/presences").contentType("application/json").content(corps("INCONNU" + i)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("CODE_INCONNU"));
        }
        // la 6e tentative — même avec un code VALIDE — est bloquée
        mvc.perform(post("/api/presences").contentType("application/json").content(corps("BLK333")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BLOCAGE_ACTIF"));
    }

    @Test
    void unCodeExpireCompteAussiCommeErreur() throws Exception {
        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                        + "values (?, ?, ?, dateadd('MINUTE', -20, current_timestamp), dateadd('MINUTE', -5, current_timestamp))",
                promo, "Ancienne", "EXP777");

        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/presences").contentType("application/json").content(corps("INCONNU" + i)))
                    .andExpect(status().isBadRequest());
        }
        // la 5e erreur est un code EXPIRÉ : le seuil est atteint
        mvc.perform(post("/api/presences").contentType("application/json").content(corps("EXP777")))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));

        mvc.perform(post("/api/presences").contentType("application/json").content(corps("BLK333")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BLOCAGE_ACTIF"));
    }

    @Test
    void apresDeuxMinutesLeBlocageEstLeve() throws Exception {
        // 5 erreurs survenues il y a 3 minutes : hors fenêtre de blocage
        Timestamp ilYaTroisMinutes = Timestamp.from(Instant.now().minusSeconds(180));
        for (int i = 0; i < 5; i++) {
            jdbc.update("insert into tentative_erreur (etudiant_id, survenue_at) values (?, ?)",
                    etudiantId, ilYaTroisMinutes);
        }

        mvc.perform(post("/api/presences").contentType("application/json").content(corps("BLK333")))
                .andExpect(status().isCreated());
    }
}
