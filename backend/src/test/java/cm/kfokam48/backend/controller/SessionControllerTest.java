package cm.kfokam48.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasLength;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * B6 — test d'intégration du contrat POST /api/sessions (issue #1, EF1/RG1) :
 * 201 avec { id, code, ouvertureAt, expirationAt }, 400 au format imposé.
 * Tourne sur un poste vierge : H2 en mémoire + Flyway, aucune base locale (B6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SessionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long promotionId;

    @BeforeEach
    void insererPromotion() {
        jdbc.update("insert into promotion (nom) values (?)", "Licence 2");
        // l'identité H2 n'est pas annulée par le rollback : on relit l'id réellement inséré
        promotionId = jdbc.queryForObject("select max(id) from promotion", Long.class);
    }

    @Test
    void creeUneSessionAvecCodeEtExpiration() throws Exception {
        mvc.perform(post("/api/sessions").contentType("application/json")
                        .content("{\"titre\":\"Algèbre\",\"promotionId\":" + promotionId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code").value(hasLength(6)))
                .andExpect(jsonPath("$.ouvertureAt").isNotEmpty())
                .andExpect(jsonPath("$.expirationAt").isNotEmpty());
    }

    @Test
    void champManquantRenvoie400AuFormatImpose() throws Exception {
        mvc.perform(post("/api/sessions").contentType("application/json").content("{\"titre\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void promotionInconnueRenvoie404AuFormatImpose() throws Exception {
        mvc.perform(post("/api/sessions").contentType("application/json")
                        .content("{\"titre\":\"x\",\"promotionId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
