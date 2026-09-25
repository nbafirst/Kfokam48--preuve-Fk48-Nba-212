-- V7 — Enveloppe étape 3 : support de deux relecteurs par exercice.
-- Ne JAMAIS modifier V6 (déjà exécutée en production).
-- Cette migration permet de passer de 1 relecture/exercice à 2 relectures/exercice.

-- 1. Supprimer l'ancienne contrainte UK (1 relecture par exercice)
ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;

-- 2. Ajouter colonne numéro de relecture (1 ou 2)
ALTER TABLE relecture ADD COLUMN numero SMALLINT NOT NULL DEFAULT 1;

-- 3. Nouvelle contrainte : max 2 relectures par exercice, numéros 1 et 2 uniques
ALTER TABLE relecture ADD CONSTRAINT uk_relecture_exercice_numero UNIQUE (exercice_id, numero);
ALTER TABLE relecture ADD CONSTRAINT chk_relecture_numero CHECK (numero IN (1, 2));

-- 4. Migrer données existantes : relectures actuelles → numero = 1
UPDATE relecture SET numero = 1 WHERE numero IS NULL;

-- 5. La colonne relecteur_id dans exercice devient obsolète (gardée pour compatibilité lecture seule)
-- Elle contiendra le premier relecteur assigné (numero = 1) pour l'affichage existant.
-- La relation complète est maintenant via la table relecture (numero 1 et 2).

-- 6. Index pour performance sur les requêtes par relecteur
CREATE INDEX IF NOT EXISTS idx_relecture_exercice_numero ON relecture (exercice_id, numero);