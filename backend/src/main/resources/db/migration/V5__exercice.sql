-- V5 — EF5 : dépôt du lien d'exercice par session.
-- Un dépôt unique par étudiant et par session (409 EXERCICE_DEJA_DEPOSE) :
-- la contrainte SQL tranche même en cas de requêtes concurrentes.
-- statut : EN_ATTENTE | ASSIGNE | RELU (voir D4) ; ASSIGNE arrive avec EF7 (#8).

CREATE TABLE exercice (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id      BIGINT        NOT NULL,
    etudiant_id     BIGINT        NOT NULL,
    lien            VARCHAR(2048) NOT NULL,
    statut          VARCHAR(12)   NOT NULL CHECK (statut IN ('EN_ATTENTE', 'ASSIGNE', 'RELU')),
    depose_at       TIMESTAMP     NOT NULL,
    lien_remplace_at TIMESTAMP    NULL,
    CONSTRAINT fk_exercice_session  FOREIGN KEY (session_id)  REFERENCES session_cours (id),
    CONSTRAINT fk_exercice_etudiant FOREIGN KEY (etudiant_id) REFERENCES etudiant (id),
    CONSTRAINT uk_exercice_session_etudiant UNIQUE (session_id, etudiant_id)
);
