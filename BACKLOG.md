# BACKLOG — Éléments Restants

Triés par priorité (Must > Should > Could > Won't) et dépendances.

---

## Must (Bloquants / Contractuels)

| ID | Titre | Description | Dépendances |
|----|-------|-------------|-------------|
| B-01 | **Frontend : Affichage PROVISIONAL/FINAL** | ÉtudiantScreen : badge statut note (PROVISIONAL = 1 éval, FINAL = 2 évals), afficher les 2 évaluations séparées quand FINAL | Backend prêt (ResultatExerciceDto enrichi) |
| B-02 | **Frontend : RelecteurScreen — numéro relecture** | Afficher "Relecture #1" / "#2" dans la liste et le formulaire (champ `numero` du DTO) | Backend prêt (RelectureEnAttenteDto.numero) |
| B-03 | **Frontend : FormateurScreen — 2 relecteurs** | Tableau de bord : afficher les 2 relecteurs assignés, leurs statuts (EN_ATTENTE/RENDUE), notes | Backend prêt (Exercice.relectures) |
| B-04 | **Frontend : Gestion erreur RELECTEUR_DEJA_ASSIGNE** | Afficher message clair si même pair tente 2e relecture (409) | Backend prêt (nouveau code erreur) |
| B-05 | **Tests E2E Docker** | Vérifier stack complète : `docker-compose up --build -d` → health checks OK → scénarios critiques | Docker images buildent |

---

## Should (Importants / Qualité)

| ID | Titre | Description | Dépendances |
|----|-------|-------------|-------------|
| B-06 | **Tests frontend (Vitest/Playwright)** | Couverture écrans critiques : présence, dépôt, relecture, résultats | B-01 à B-04 |
| B-07 | **Accessibilité (WCAG 2.1 AA)** | Contraste, focus visible, ARIA labels, navigation clavier | B-01 à B-04 |
| B-08 | **Validation côté client** | Vérification note 0-20, lien HTTP(S), code présence format avant envoi | B-01 à B-04 |
| B-09 | **Pagination / recherche** | Liste sessions, présents, exercices (si > 50 items) | — |
| B-10 | **Internationalisation (i18n)** | FR/EN pour messages UI, erreurs API | — |

---

## Could (Améliorations / Confort)

| ID | Titre | Description | Dépendances |
|----|-------|-------------|-------------|
| B-11 | **Notifications temps réel (WebSocket/SSE)** | Push : nouveau code présence, relecture assignée, note rendue | Backend : WebSocket config |
| B-12 | **Export CSV/PDF tableau formateur** | Bouton export résultats session | B-03 |
| B-13 | **Mode hors-ligne (PWA)** | Service Worker, cache API, sync différée | — |
| B-14 | **Thème sombre/clair** | Toggle CSS variables, persistance localStorage | — |
| B-15 | **Analytics anonymes** | Métriques usage (dépôts, relectures, temps) | — |

---

## Won't (Hors Périmètre Examen)

| ID | Titre | Raison |
|----|-------|--------|
| W-01 | Authentification JWT/OAuth | Non requis par le cahier des charges (identification par ID seulement) |
| W-02 | Multi-tenancy / multi-écoles | Périmètre unique session |
| W-03 | API GraphQL | REST suffisant, contrat imposé OpenAPI |
| W-04 | Base de données NoSQL | Relationnel imposé (PostgreSQL/H2) |
| W-05 | Kubernetes / Helm | Docker-compose suffit pour l'épreuve |

---

## Notes Techniques

### Frontend — Points d'intégration Part B
```javascript
// api.js — déjà mis à jour pour nouveaux champs
// ResultatExerciceDto: { statutNote, nbEvaluations, moyenne, evaluations: [{note, commentaire}] }
// RelectureEnAttenteDto: { numero: 1|2 }

// ÉtudiantScreen.jsx
if (resultat.statutNote === 'FINAL') {
  // Afficher les 2 évaluations côte à côte
  resultat.evaluations.map(e => ...)
} else if (resultat.statutNote === 'PROVISIONAL') {
  // Afficher note unique + badge "Provisoire"
}

// RelecteurScreen.jsx
// relecture.numero === 1 ? "Première relecture" : "Deuxième relecture"

// FormateurScreen.jsx
// exercice.relectures.map(r => <span>#{r.numero} - {r.statut}</span>)
```

### Backend — Déjà Implémenté (v1.0)
- Migration V7 appliquée automatiquement au démarrage (dev) ou via `ddl-auto=update` (prod)
- Tous les endpoints renvoient les nouveaux champs
- 26 tests passent

---

## Prochaines Actions Recommandées

1. **Immédiat** : B-01 à B-04 (frontend Part B) — 2-3h
2. **Court terme** : B-05 (validation Docker E2E) — 30min
3. **Si temps** : B-06, B-07 (tests + a11y) — 2-4h