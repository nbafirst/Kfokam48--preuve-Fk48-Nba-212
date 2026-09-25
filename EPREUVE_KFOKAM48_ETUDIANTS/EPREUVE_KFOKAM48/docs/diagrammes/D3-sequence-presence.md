# D3 — Séquence : « marquer sa présence »

Cas nominal et erreurs, codes HTTP strictement identiques à `api/contrat.yaml` (`POST /api/presences`) : `201` / `400 CODE_INCONNU` / `409 DEJA_PRESENT` / `410 CODE_EXPIRE`. Règles citées : RG1 (expiration 15 min), RG2 (unicité), RG3 (blocage 5 erreurs / 2 min).

```mermaid
sequenceDiagram
    autonumber
    participant E as Étudiant
    participant F as Frontend
    participant C as PresenceController
    participant S as PresenceService
    participant R as PresenceRepository

    E->>F: saisit le code (mobile, ENF1)
    F->>C: POST /api/presences { code, etudiantId }

    alt blocage actif — 5 erreurs en moins de 2 min (RG3)
        S->>S: compteur d'erreurs de l'étudiant ≥ 5
        S-->>C: BlocageActifException
        C-->>F: 400 { code: "BLOCAGE_ACTIF", message: "Réessayez dans 2 minutes." }
    else code inconnu (RG1)
        S->>R: chercher la session par code
        R-->>S: vide
        S-->>C: CodeInconnuException
        C-->>F: 400 { code: "CODE_INCONNU" }
    else code expiré (RG1)
        S->>R: chercher la session par code
        R-->>S: session (expirationAt < maintenant)
        S-->>C: CodeExpireException
        C-->>F: 410 { code: "CODE_EXPIRE" }
    else étudiant déjà présent (RG2)
        S->>R: existsBySessionIdAndEtudiantId
        R-->>S: true
        S-->>C: DejaPresentException
        C-->>F: 409 { code: "DEJA_PRESENT" }
    else cas nominal
        S->>R: existsBySessionIdAndEtudiantId
        R-->>S: false
        S->>R: save(Presence{ source: ETUDIANT })
        R-->>S: présence enregistrée (RG2 tenu par la contrainte SQL)
        S-->>C: Presence
        C-->>F: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
        F-->>E: confirmation « présence enregistrée »
    end
```

Chaque branche d'erreur est gérée par le `@RestControllerAdvice` (B4) : le format d'erreur imposé `{ code, message }` est le même partout, aucune stack trace ne sort. Le cas « session clôturée » n'apparaît pas ici : une session clôturée a nécessairement son code expiré (RG1) — l'erreur renvoyée est donc `CODE_EXPIRE`.
