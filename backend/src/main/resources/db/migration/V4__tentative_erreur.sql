-- V4 — RG3 (Q4) : compteur des codes erronés par étudiant.
-- Une ligne par tentative en erreur (code inconnu ou expiré) ; le service
-- bloque au-delà de 5 lignes sur une fenêtre glissante de 2 minutes.

CREATE TABLE tentative_erreur (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id BIGINT    NOT NULL,
    survenue_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tentative_etudiant FOREIGN KEY (etudiant_id) REFERENCES etudiant (id)
);

CREATE INDEX idx_tentative_etudiant_date ON tentative_erreur (etudiant_id, survenue_at);
