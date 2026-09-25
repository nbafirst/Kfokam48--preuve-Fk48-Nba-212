package cm.kfokam48.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #3 — RG2 (une seule présence par session) et ENF3.
 *
 * L'unicité tient sous requêtes concurrentes parce qu'elle est portée par la
 * contrainte SQL uk_presence_session_etudiant (migration V3), pas seulement par
 * la garde applicative : on le prouve en tentant un doublon directement en SQL
 * (c'est ce que produiraient deux requêtes HTTP simultanées qui passeraient la
 * garde en même temps) et en vérifiant que la base la refuse.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenceUniciteTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private String code;
    private long etudiantId;
    private long sessionId;

    @BeforeEach
    void preparerScenario() {
        jdbc.update("insert into promotion (nom) values (?)", "Licence 2");
        long promo = jdbc.queryForObject("select max(id) from promotion", Long.class);
        jdbc.update("insert into etudiant (promotion_id, nom) values (?, ?)", promo, "Nadège");
        etudiantId = jdbc.queryForObject("select max(id) from etudiant", Long.class);
        jdbc.update("insert into session_cours (promotion_id, titre, code, ouverture_at, expiration_at) "
                + "values (?, ?, ?, current_timestamp, dateadd('MINUTE', 10, current_timestamp))", promo, "Algèbre", "UNI222");
        code = "UNI222";
        sessionId = jdbc.queryForObject("select max(id) from session_cours", Long.class);
    }

    @Test
    void unDeuxiemeMarquageRenvoie409SansDoublon() throws Exception {
        String corps = "{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}";

        mvc.perform(post("/api/presences").contentType("application/json").content(corps))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/presences").contentType("application/json").content(corps))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));

        Long nb = jdbc.queryForObject("select count(*) from presence", Long.class);
        assertThat(nb).isEqualTo(1L);
    }

    @Test
    void laContrainteSqlRefuseLeDoublonMemeHorsApplication() {
        // première présence insérée "hors application"
        jdbc.update("insert into presence (session_id, etudiant_id, source, cree_at) values (?, ?, 'ETUDIANT', ?)",
                sessionId, etudiantId, Timestamp.from(Instant.now()));

        // tentative de doublon directe en SQL : ce que produiraient deux requêtes
        // simultanées passant la garde applicative en même temps
        assertThatThrownBy(() -> jdbc.update(
                        "insert into presence (session_id, etudiant_id, source, cree_at) values (?, ?, 'FORMATEUR', ?)",
                        sessionId, etudiantId, Timestamp.from(Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);

        Long nb = jdbc.queryForObject("select count(*) from presence", Long.class);
        assertThat(nb).isEqualTo(1L);
    }
}
