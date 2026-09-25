# Soumission — Épreuve finale fullstack KFOKAM48

> Remplis ce fichier, **vérifie tes deux liens depuis une fenêtre de navigation privée**,
> puis téléverse-le sur la plateforme **avant 18h00**.
> Sans ce dépôt sur la plateforme, tu n'as rien rendu.

---

## Candidat

| | |
|---|---|
| Nom et prénom(s) | Mbouango Nkeme Boris Arsene |
| Matricule | KF48-_nba_-212 |
| Centre | Yaoundé / Douala / Bafoussam |
| Compte GitHub | nbafirst |

## Projet

| | |
|---|---|
| Dépôt (public) | `git@github.com:nbafirst/Kfokam48--preuve-Fk48-Nba-212.git /  https://github.com/nbafirst/Kfokam48--preuve-Fk48-Nba-212` |
| Commit final — hash complet, 40 caractères | 5ba8bdc32dfd0f1f4cf49065cb6075161963b8a1 |
| Branche | `main` |

## Épreuve Git — étape 5

| | |
|---|---|
| Dépôt (public) | `https://github.com/nbafirst/kfokam48-gitlab-KF48-_nba_-212` |
| Commit final — hash complet, 40 caractères | 5ba8bdc32dfd0f1f4cf49065cb6075161963b8a1 |

## Technique

| | |
|---|---|
| Frontend utilisé | React (Vite 5) |
| Base de données | PostgreSQL 16 (prod) / H2 (dev/tests) |
| Commandes de démarrage | `docker-compose up --build -d` (ports 8081 backend, 8082 frontend) |

## Ce que j'ai livré

- Backend Spring Boot 4.1.1 (Java 17) avec 26 tests d'intégration validés
- Frontend React/Vite mobile-first (360px min) : 3 écrans Formateur/Étudiant/Relecteur
- Docker multi-stage (backend) + nginx (frontend) + PostgreSQL via docker-compose
- Architecture BDD-driven : Flyway migrations V1-V7, profiles dev/prod
- ÉTAPE 3 Part B implémentée : 2 relecteurs distincts par exercice (numero 1/2), notes PROVISIONAL/FINAL, moyenne des 2 notes, RG17 (même pair exclu), API enrichie
- 3 jalons Git : `[JALON] v0.1` (socle), `[JALON] v1.0` (2 relecteurs), merge sur main

---

## Avant de téléverser, vérifie

- [x] Mes deux dépôts sont **publics** et s'ouvrent en navigation privée
- [x] Les deux hash font bien **40 caractères** et existent sur GitHub
- [x] Tout mon travail est **poussé** — `git status` est propre sur les deux dépôts
- [x] Mon `README` a été testé depuis un clone vierge, dans un dossier vide
- [x] Mon `JOURNAL.md` et mon cahier des charges sont dans `docs/`
- [x] Les trois commits `[JALON]` sont poussés et dans le bon ordre

---

**Déclaration.** J'ai réalisé ce travail seul. Les outils d'IA étaient autorisés sans restriction et je les ai utilisés ; mon journal indique où et comment j'ai vérifié leurs réponses. Mes dépôts resteront publics et inchangés jusqu'à la publication des résultats.

Signature : ______________________  Date : __________

---

## Commandes de lancement

### Via Docker (recommandé - production)

```bash
# Cloner le dépôt
git clone https://github.com/nbafirst/Kfokam48--preuve-Fk48-Nba-212.git
cd Kfokam48--preuve-Fk48-Nba-212

# Construire et démarrer la stack complète (PostgreSQL + Backend + Frontend)
docker-compose up --build -d

# Vérifier les logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Accéder à l'application
# Frontend : http://localhost:8082
# Backend API : http://localhost:8081/api
# Swagger UI : http://localhost:8081/swagger-ui.html
```

### Développement local (sans Docker)

**Backend (nécessite Java 17 + Maven) :**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# API sur http://localhost:8081/api (H2 en mémoire)
# Console H2 : http://localhost:8081/h2-console (JDBC: jdbc:h2:mem:kfokam48)
```

**Frontend (nécessite Node.js 20+) :**
```bash
cd frontend
npm install
npm run dev
# Frontend sur http://localhost:5173 (proxy vers backend :8081)
```

### Tests

```bash
# Tests backend (26 tests d'intégration)
cd backend
./mvnw test

# Build backend (JAR exécutable)
./mvnw package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Build frontend (production)
cd frontend
npm run build
# Servir dist/ avec nginx ou `npx serve dist`
```

### Arrêt et nettoyage Docker

```bash
# Arrêter la stack
docker-compose down

# Arrêter + supprimer volumes (remise à zéro BDD)
docker-compose down -v
```