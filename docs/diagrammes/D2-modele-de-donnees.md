# D2 — Modèle de données

Entités, attributs et cardinalités qui devront correspondre **exactement** aux migrations Flyway de l'étape 2. Le relecteur n'est pas une table (hypothèse H2) : `RELECTURE` relie deux étudiants via un exercice.

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "contient"
    PROMOTION ||--o{ SESSION : "organise"
    SESSION ||--o{ PRESENCE : "recueille"
    ETUDIANT ||--o{ PRESENCE : "marque"
    SESSION ||--o{ EXERCICE : "recueille"
    ETUDIANT ||--o{ EXERCICE : "depose"
    EXERCICE ||--o| RELECTURE : "recu 1 seule"
    ETUDIANT ||--o{ RELECTURE : "effectue"

    PROMOTION {
        bigint id PK
        varchar nom
    }
    ETUDIANT {
        bigint id PK
        bigint promotion_id FK
        varchar nom
    }
    SESSION {
        bigint id PK
        bigint promotion_id FK
        varchar titre
        varchar code
        timestamp ouverture_at
        timestamp expiration_at
        timestamp cloture_at "NULL si non clôturée"
    }
    PRESENCE {
        bigint id PK
        bigint session_id FK
        bigint etudiant_id FK "UK (session_id, etudiant_id) — RG2"
        varchar source "ETUDIANT | FORMATEUR — RG12"
        timestamp cree_at
    }
    EXERCICE {
        bigint id PK
        bigint session_id FK
        bigint etudiant_id FK "UK (session_id, etudiant_id) : 1 dépôt max"
        varchar lien
        varchar statut "EN_ATTENTE | ASSIGNE | RELU"
        timestamp depose_at
        timestamp lien_remplace_at "NULL si jamais remplacé"
    }
    RELECTURE {
        bigint id PK
        bigint exercice_id FK "UK : un seul relecteur — RG5"
        bigint relecteur_id FK "≠ auteur — RG4"
        int note "entier 0..20 — RG7"
        varchar commentaire
        varchar statut "ASSIGNEE | RENDUE"
        timestamp assignee_at
        timestamp rendue_at
    }
```

Choisis structurants :
- **RG2** (unicité de présence) et **un dépôt par session** sont tenus par des contraintes d'unicité SQL, pas seulement par le code applicatif (ENF3).
- **RG5** (un seul relecteur) : contrainte d'unicité sur `relecture.exercice_id`.
- **RG4** (pas d'auto-relecture) : vérifiée au service, traduite en `403 AUTO_RELECTURE` (contrat).
- Le **statut de l'exercice** (D4) est dérivé de `relecture.statut` : `EN_ATTENTE` → `ASSIGNE` (relecteur désigné) → `RELU` (note rendue).
- La **moyenne** (RG15) n'est stockée nulle part : elle est calculée par l'API dans `GET /api/tableau` (F3 du sujet).
