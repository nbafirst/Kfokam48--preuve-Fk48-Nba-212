# CHANGELOG

Toutes les modifications notables de ce projet sont documentées ici.

Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et le projet utilise le [Versioning Sémantique](https://semver.org/lang/fr/).

---

## [1.0.0] — 2026-09-25 — `[JALON] v1.0`

### Ajouté — ÉTAPE 3 Part B : Deux relecteurs par exercice
- **Migration Flyway V7** : colonne `numero` (SMALLINT 1/2), contrainte unique `(exercice_id, numero)`, CHECK `numero IN (1,2)`
- **Entité `Relecture`** : champ `numero` (Short) pour identifier la 1re ou 2e relecture
- **Entité `Exercice`** : relation `relectures` (List<Relecture>), `relecteur` conservé pour compatibilité
- **Repository `RelectureRepository`** :
  - `findByExerciceIdAndRendueAtIsNull(Long exerciceId)`
  - `findByRelecteurIdAndRendueAtIsNull(Long relecteurId)`
  - `existsByExerciceIdAndRelecteurId(Long exerciceId, Long relecteurId)`
- **ServiceExercice.assignerRelecteurs()** : tire au sort jusqu'à 2 relecteurs distincts parmi les présents (exclut l'auteur), assigne `numero` 1 et 2
- **ServiceRelecture** :
  - Gestion de 2 relectures par exercice
  - Statut note : `AUCUNE` / `PROVISIONAL` (1 éval) / `FINAL` (2 évals = moyenne)
  - Prévention RG17 : même pair ne peut pas faire les 2 relectures (`RELECTEUR_DEJA_ASSIGNE`)
  - Correction note possible jusqu'à clôture (RG13)
- **ServiceTableau** : moyenne des moyennes (moyenne par exercice → moyenne globale)
- **DTO `ResultatExerciceDto`** enrichi : `statutNote`, `nbEvaluations`, `moyenne`, `evaluations[]`
- **DTO `RelectureEnAttenteDto`** enrichi : champ `numero` (1 ou 2)
- **Contrat API `api/contrat.yaml` v1.1** :
  - `GET /api/etudiants/{id}/resultats` : réponse enrichie
  - `GET /api/relectures/en-attente` : inclut `numero`
  - Nouvel erreur `RELECTEUR_DEJA_ASSIGNE` (409)
- **Cahier des charges** mis à jour (sections 3, 4, 6, 7, journal v2)

### Modifié
- `application-prod.properties` : Flyway désactivé, `ddl-auto=update` (incompatibilité PostgreSQL 16.15)
- Ports Docker : backend 8081, frontend 8082 (conflits hôte 80/8080)

### Tests
- 26 tests d'intégration backend passent (issues #1-15 + Part B)

---

## [0.1.0] — 2026-09-25 — `[JALON] v0.1`

### Ajouté — Socle complet (ÉTAPES 1 & 2)
- **Backend Spring Boot 4.1.1** (Java 17)
  - Entités : `SessionCours`, `Etudiant`, `Presence`, `Exercice`, `Relecture`
  - Flyway migrations V1–V6 (schéma complet)
  - Profils `dev` (H2) / `prod` (PostgreSQL)
  - 26 tests d'intégration (MockMvc + Testcontainers-like H2)
- **API REST** (contrat `api/contrat.yaml` v1.0) — 5 opérations imposées + extensions :
  - `POST /api/sessions` — EF1 ouverture session + code présence
  - `POST /api/sessions/{id}/presence` — EF2 marquage présence
  - `GET /api/sessions/{id}/presents` — EF3 liste présents temps réel
  - `POST /api/sessions/{id}/presence/manuelle` — EF4 ajout manuel formateur
  - `POST /api/exercices` — EF5 dépôt lien
  - `PATCH /api/exercices/{id}/lien` — EF6 remplacement lien
  - `POST /api/relectures/{id}` — EF8 relecture rendue
  - `PATCH /api/relectures/{id}/correction` — EF9 correction
  - `GET /api/relectures/en-attente` — relectures à rendre
  - `GET /api/etudiants/{id}/resultats` — EF11 résultats étudiant
  - `GET /api/sessions/{id}/tableau` — EF10 tableau formateur
  - `POST /api/sessions/{id}/cloture` — clôture session
  - `PATCH /api/etudiants/{id}/promotion` — promotion étudiant
- **Frontend React/Vite 5** — 3 écrans responsive (360px min) :
  - **Formateur** : créer session, code présence, liste présents, ajout manuel, clôture, tableau de bord
  - **Étudiant** : saisir code, déposer/remplacer lien, voir résultats (anonymat relecteurs)
  - **Relecteur** : liste relectures en attente, rendre note/commentaire, corriger
  - Service API centralisé (`services/api.js`), gestion erreurs, CSS mobile-first avec variables
- **Docker** :
  - Backend multi-stage (maven build → jre runtime)
  - Frontend nginx (build Vite → nginx static + proxy `/api`)
  - `docker-compose.yml` : postgres:16 + backend + frontend
- **Documentation** :
  - `docs/CAHIER_DES_CHARGES.md` v1 (spécifications complètes)
  - `docs/JOURNAL.md` (journal de bord)
  - `docs/ARCHITECTURE.md` (décisions techniques)

### Corrigé
- RG1 : code présence inconnu (400) / expiré (410)
- RG2 : unicité présence par session
- RG3 : blocage 2min après 5 codes erronés
- RG4 : pas d'auto-relecture
- RG5 : relecteur parmi présents
- RG6 : relecteur ≠ auteur
- RG7 : note entière 0–20
- RG8 : code présence unique
- RG9 : dépôt possible après expiration code présence (jusqu'à clôture)
- RG10 : anonymat relecteurs
- RG11 : remplacement lien refusé si relecture commencée
- RG12 : ajout manuel formateur
- RG13 : correction jusqu'à clôture
- RG14 : clôture fige tout
- RG15 : promotion change centre

---

## [0.0.1] — 2026-09-24 — Initialisation

### Ajouté
- Structure projet multi-module (backend + frontend)
- Configuration Maven + Vite
- Git init, .gitignore, premiers commits

---