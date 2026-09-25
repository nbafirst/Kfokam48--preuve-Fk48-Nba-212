# KFOKAM48 — Épreuve Fullstack

Application de gestion de présence et relecture par les pairs pour l'épreuve KFOKAM48.

## Stack Technique

- **Backend** : Spring Boot 4.1.1 (Java 17), H2 (dev/tests), PostgreSQL 16 (prod), Flyway migrations
- **Frontend** : React 18 + Vite 5, mobile-first (360px min), CSS custom properties
- **Infrastructure** : Docker multi-stage (backend), nginx (frontend), docker-compose
- **API** : REST, contrat OpenAPI `api/contrat.yaml` v1.1

## Démarrage Rapide (Docker — Recommandé)

```bash
git clone https://github.com/nbafirst/Kfokam48--preuve-Fk48-Nba-212.git
cd Kfokam48--preuve-Fk48-Nba-212
docker-compose up --build -d
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:8082 |
| Backend API | http://localhost:8081/api |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Health | http://localhost:8081/actuator/health |

> **Ports** : 8081 (backend), 8082 (frontend) — changés car 80/8080 occupés sur l'hôte.

## Développement Local

### Backend
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# API: http://localhost:8081/api (H2 en mémoire)
# H2 Console: http://localhost:8081/h2-console (JDBC: jdbc:h2:mem:kfokam48)
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# App: http://localhost:5173 (proxy vers backend :8081)
```

## Tests

```bash
cd backend
./mvnw test          # 26 tests d'intégration
./mvnw package -DskipTests  # JAR exécutable
```

## Architecture

```
├── api/contrat.yaml           # Contrat OpenAPI v1.1
├── backend/
│   ├── src/main/java/...      # Spring Boot app
│   ├── src/main/resources/
│   │   ├── application.yml           # Config commune
│   │   ├── application-dev.yml       # H2, Flyway activé
│   │   ├── application-prod.yml      # PostgreSQL, Flyway désactivé
│   │   └── db/migration/V*.sql       # Flyway V1–V7
│   └── Dockerfile             # Multi-stage (build + runtime)
├── frontend/
│   ├── src/
│   │   ├── screens/           # Formateur, Étudiant, Relecteur
│   │   ├── services/api.js    # Client API centralisé
│   │   └── styles/            # CSS mobile-first, variables
│   ├── nginx.conf             # Proxy /api -> backend:8081
│   └── Dockerfile             # nginx + build Vite
├── docker-compose.yml         # postgres + backend + frontend
└── docs/
    ├── CAHIER_DES_CHARGES.md  # Spécifications complètes
    ├── JOURNAL.md             # Journal de bord
    └── ARCHITECTURE.md        # Décisions techniques
```

## Fonctionnalités Clés

### ÉTAPE 1 — Session & Présence
- EF1: Ouverture session avec code présence (RG1: code unique, expire 2min)
- EF2: Marquage présence nominale (RG2: unicité, RG3: blocage 2min après 5 erreurs)
- EF3: Liste présents temps réel
- EF4: Ajout manuel présence formateur (RG12)

### ÉTAPE 2 — Exercice & Relecture
- EF5: Dépôt lien exercice (jusqu'à clôture session, RG9)
- EF6: Remplacement lien (RG11: refusé si relecture commencée)
- EF7: Assignation **2 relecteurs distincts** parmi présents (RG5, RG17: même pair exclu)
- EF8: Relecture rendue (note 0–20, commentaire, RG7)
- EF9: Correction relecture (RG13: jusqu'à clôture)
- EF10: Tableau de bord formateur (statuts, moyennes)
- EF11: Résultats étudiant (anonymat relecteurs RG10, notes PROVISIONAL/FINAL RG16)

### ÉTAPE 3 — Extras
- Promotion étudiant (changement centre, RG15)
- Clôture session (RG14: fige tout)
- Deux relecteurs par exercice (Part B): `numero` 1/2, note PROVISIONAL (1 éval) / FINAL (moyenne 2 évals)

## Profils & Configuration

| Profil | BDD | Flyway | Usage |
|--------|-----|--------|-------|
| `dev` (défaut) | H2 mémoire | Activé | Tests, dev local |
| `prod` | PostgreSQL | Désactivé* | Docker, production |

> *Flyway désactivé en prod (incompatibilité PostgreSQL 16.15) → `spring.jpa.hibernate.ddl-auto=update`

## Jalons Git

| Tag | Commit | Description |
|-----|--------|-------------|
| `[JALON] v0.1` | `64cc0f8` | Socle complet : backend (issues #1-11), frontend 3 écrans, Docker, API v1.0 |
| `[JALON] v1.0` | `78e2b73` | **2 relecteurs/exercice** (Part B ÉTAPE 3) : migration V7, PROVISIONAL/FINAL, RG17, API enrichie |

## Commandes Utiles

```bash
# Logs Docker
docker-compose logs -f backend
docker-compose logs -f frontend

# Arrêt propre
docker-compose down

# Reset complet (BDD + volumes)
docker-compose down -v

# Build images sans démarrer
docker-compose build
```

## Licence

Projet d'examen KFOKAM48 — usage éducatif.