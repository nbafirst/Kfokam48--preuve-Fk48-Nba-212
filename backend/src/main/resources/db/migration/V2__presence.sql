-- V2 — Table presence (issue #2, EF2)
-- La contrainte d'unicité (session_id, etudiant_id) arrive en V3 (issue #3, RG2/ENF3).

CREATE TABLE presence (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  BIGINT      NOT NULL,
    etudiant_id BIGINT      NOT NULL,
    source      VARCHAR(12) NOT NULL CHECK (source IN ('ETUDIANT', 'FORMATEUR')),
    cree_at     TIMESTAMP   NOT NULL,
    CONSTRAINT fk_presence_session  FOREIGN KEY (session_id)  REFERENCES session_cours (id),
    CONSTRAINT fk_presence_etudiant FOREIGN KEY (etudiant_id) REFERENCES etudiant (id)
);
