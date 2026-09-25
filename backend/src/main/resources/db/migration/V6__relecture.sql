-- V6 — EF7/EF8 : relecture assignée au hasard (EF7) et rendue (EF8).
-- Un seul relecteur par exercice (RG5) : contrainte unique sur exercice_id.
-- Le relecteur est un étudiant présent à la session (RG4, RG6).
-- statut de l'exercice : EN_ATTENTE | ASSIGNE | RELU (D4).
-- Ajout de la colonne relecteur_id dans la table exercice.

CREATE TABLE relecture (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    exercice_id     BIGINT        NOT NULL,
    relecteur_id    BIGINT        NOT NULL,
    note            INTEGER       NULL,
    commentaire     VARCHAR(2048) NULL,
    rendue_at       TIMESTAMP     NULL,
    CONSTRAINT fk_relecture_exercice    FOREIGN KEY (exercice_id)  REFERENCES exercice (id),
    CONSTRAINT fk_relecture_relecteur   FOREIGN KEY (relecteur_id) REFERENCES etudiant (id),
    CONSTRAINT uk_relecture_exercice    UNIQUE (exercice_id)
);

CREATE INDEX idx_relecture_relecteur ON relecture (relecteur_id);

ALTER TABLE exercice ADD COLUMN relecteur_id BIGINT NULL;
ALTER TABLE exercice ADD CONSTRAINT fk_exercice_relecteur FOREIGN KEY (relecteur_id) REFERENCES etudiant (id);