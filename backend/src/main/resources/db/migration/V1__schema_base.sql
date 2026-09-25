-- V1 — Schéma de base : promotion, etudiant, session_cours
-- Conforme au diagramme D2 (docs/diagrammes/D2-modele-de-donnees.md).
-- Le nom de table session_cours evite le mot-cle SQL "SESSION".

CREATE TABLE promotion (
    id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(120) NOT NULL
);

CREATE TABLE etudiant (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id BIGINT       NOT NULL,
    nom          VARCHAR(120) NOT NULL,
    CONSTRAINT fk_etudiant_promotion FOREIGN KEY (promotion_id) REFERENCES promotion (id)
);

CREATE TABLE session_cours (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    promotion_id  BIGINT       NOT NULL,
    titre         VARCHAR(180) NOT NULL,
    code          VARCHAR(12)  NOT NULL,
    ouverture_at  TIMESTAMP    NOT NULL,
    expiration_at TIMESTAMP    NOT NULL,
    cloture_at    TIMESTAMP    NULL,
    CONSTRAINT fk_session_promotion FOREIGN KEY (promotion_id) REFERENCES promotion (id),
    CONSTRAINT uk_session_code      UNIQUE (code)
);
