-- V3 — RG2/ENF3 : un étudiant ne peut marquer qu'une seule présence par session.
-- La contrainte SQL tranche même entre deux requêtes concurrentes.

ALTER TABLE presence ADD CONSTRAINT uk_presence_session_etudiant UNIQUE (session_id, etudiant_id);
