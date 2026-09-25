# Cahier des charges — KF48 Présences & Relectures

**Auteur :** MBOUANGO NKEM BORIS ARSENE · FK48-212
**Version :** 1 · **Date :** 2026-09-25
**Frontend choisi :** React, parce que l'équipe maîtrise son écosystème et que le sujet impose trois écrans simples ; un seul bundle Vite suffit pour les trois rôles.

> Document rédigé à partir de `SUJET.md` et de `CLIENT.md` (16 questions `Qx`).
> Toute décision prise à la place du client cite sa source (`Qx`, `RGx`).
> Document à mettre à jour après l'ouverture de l'enveloppe (étape 3) — voir journal des révisions.

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 suit à la main trois choses qui se recoupent : qui était présent en session, qui a rendu son exercice, et qui a relu l'exercice de qui. Cette application remplace ce suivi manuel : le formateur ouvre une session et obtient un code de présence ; l'étudiant marque sa présence avec ce code, dépose le lien de son exercice ; le système assigne au hasard un pair présent pour relire l'exercice et lui donner une note sur 20 avec un commentaire ; le formateur consulte un tableau récapitulatif par étudiant. L'objectif est que chaque information (présence, dépôt, note) soit saisie une fois, par la bonne personne, au bon moment — et visible immédiatement dans le tableau du formateur.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| **Formateur** | Ouvrir une session (obtient un code de présence), consulter le tableau récapitulatif, clôturer une session, ajouter manuellement une présence marquée « ajouté par le formateur » (Q14) | Marquer sa propre présence, déposer un exercice, relire, modifier une note ou un commentaire |
| **Étudiant** | Marquer sa présence avec le code (Q2, Q3), déposer le lien de son exercice jusqu'à la clôture (Q12), remplacer son lien tant que personne n'a commencé à le relire (Q13), consulter la note et le commentaire reçus sans voir le nom du relecteur (Q8) | Marquer sa présence deux fois (RG2), déposer deux exercices par session, se relire lui-même (Q5), modifier une présence, changer son lien après le début d'une relecture (Q13) |
| **Relecteur** | Rendre une note entière sur 20 et un commentaire (Q9), corriger sa note tant que le formateur n'a pas clôturé (Q10) | Relire son propre exercice (Q5), rendre deux fois la même relecture, relecture d'un exercice hors périmètre |

**Décision structurante (section 7, hypothèse H2) :** le relecteur n'est **pas** un acteur distinct dans le modèle de données : c'est un étudiant dans l'état « assigné à une relecture ». Il y a donc deux tables d'acteurs seulement (`Etudiant`, `Formateur`), et la relecture est une relation entre deux étudiants via un exercice.

## 3. Périmètre

**Inclus dans cette version :**
- Ouverture de session par le formateur avec génération de code de présence (EF1)
- Marquage de présence par code, avec expiration 15 min (EF2, RG1) et unicité (RG2)
- Blocage 2 minutes après 5 codes erronés (EF3, RG3)
- Ajout manuel de présence par le formateur, marquée « ajouté par le formateur » (EF4, Q14)
- Dépôt d'un lien d'exercice par session (EF5), remplacement du lien tant que la relecture n'a pas commencé (EF6, Q13)
- Assignation aléatoire d'un relecteur parmi les présents (EF7, Q7)
- Relecture avec note entière 0–20 et commentaire (EF8, Q9), correction tant que la session n'est pas clôturée (EF9, Q10)
- Tableau du formateur : présences, dépôts, moyenne des notes reçues, relectures en attente (EF10, Q16)
- Consultation par l'étudiant de sa note et de son commentaire, sans nom du relecteur (EF11, Q8)
- Clôture de session par le formateur, qui fige présences, dépôts et notes (EF12, Q3, Q10, Q12)

**Explicitement exclu :**
- Authentification par mot de passe : l'étudiant se choisit dans une liste (Q1)
- Deuxième relecteur par exercice (Q6 : un seul)
- Notation du relecteur ou du commentaire par le formateur
- Dépôt de fichiers joints : seul un lien est déposé
- Notifications (e-mail, SMS, push) de toute nature
- Gestion des promotions elle-même (création/édition) : les promotions et les étudiants existent en base, préchargés
- Application mobile native ; l'usage mobile passe par le responsive web (ENF1)
- Paiement, multilingue, import/export CSV, statistiques au-delà du tableau imposé

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session et obtient un code de présence | Quand le formateur crée une session avec un titre et une promotion, il reçoit un code et une expiration à `ouvertureAt + 15 min` (RG1) ; ce code permet de marquer une présence tant qu'il n'a pas expiré | Must |
| EF2 | L'étudiant marque sa présence avec le code | Quand un étudiant présent dans la promotion saisit un code valide et non expiré, sa présence est enregistrée avec `source=ETUDIANT` et apparaît dans le tableau du formateur | Must |
| EF3 | Le système bloque un étudiant après 5 codes erronés | Quand un même étudiant envoie 5 codes inconnus ou expirés, toute nouvelle tentative est refusée pendant 2 minutes avec un message dédié | Must |
| EF4 | Le formateur ajoute une présence à la main | Quand le formateur ajoute un étudiant absent, la présence est créée avec `source=FORMATEUR` et « ajouté par le formateur » apparaît dans le tableau | Must |
| EF5 | L'étudiant dépose le lien de son exercice | Quand un étudiant dépose un lien valide pour une session non clôturée, l'exercice passe en `EN_ATTENTE` ; un second dépôt est refusé | Must |
| EF6 | L'étudiant remplace son lien tant que personne n'a commencé à le relire | Quand le relecteur n'a pas encore rendu sa note, le dépôt d'un nouveau lien remplace l'ancien ; après le début de la relecture, le remplacement est refusé (Q13) | Should |
| EF7 | Le système assigne un relecteur au hasard parmi les étudiants présents à la session | Quand un exercice est déposé, un relecteur est choisi au hasard parmi les étudiants présents, à l'exclusion de l'auteur ; l'exercice passe en `ASSIGNE` | Must |
| EF8 | Le relecteur rend une note entière sur 20 et un commentaire | Quand le relecteur envoie une note entière entre 0 et 20 et un commentaire, l'exercice passe en `RELU` ; l'auteur voit la note et le commentaire sans le nom du relecteur | Must |
| EF9 | Le relecteur corrige sa note tant que la session n'est pas clôturée | Quand le relecteur renvoie une note corrigée avant la clôture, la note est mise à jour ; après clôture, la correction est refusée (Q10, tranché contre Q15) | Must |
| EF10 | Le formateur consulte le tableau récapitulatif | Pour chaque étudiant de la promotion : nombre de présences, nombre d'exercices déposés, moyenne des notes reçues (nulle si aucune note), nombre de relectures qu'il doit encore rendre ; la moyenne est calculée par l'API, jamais par le frontend | Must |
| EF11 | L'étudiant consulte la note et le commentaire reçus | Quand une relecture est rendue, l'auteur de l'exercice voit la note et le commentaire, sans jamais voir le nom du relecteur (Q8) | Should |
| EF12 | Le formateur clôture la session | Quand le formateur clôture, plus aucune présence, dépôt, remplacement de lien ou correction de note n'est accepté ; les relectures jamais rendues restent visibles « en attente » dans le tableau (Q3, Q11, Q12) | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | Le marquage de présence est utilisable sur un téléphone en fin de séance : formulaire à un champ (le code) et un bouton | Test manuel sur un écran de 360 px de large : aucun scroll horizontal, bouton principal accessible au pouce |
| ENF2 | `POST /api/presences` répond en moins de 500 ms pour 60 étudiants validant dans la même minute | Test d'intégration simulant 60 requêtes simultanées ; p95 < 500 ms |
| ENF3 | L'unicité de présence (RG2) tient sous requêtes concurrentes : pas de doublon possible en base | Contrainte d'unicité SQL `(session_id, etudiant_id)` ; test d'intégration avec deux requêtes parallèles → un seul enregistrement, une réponse 409 |
| ENF4 | Aucune stack trace ni page d'erreur Spring n'est renvoyée : le format d'erreur imposé s'applique à toutes les erreurs, sans exception | Tests d'intégration sur chaque erreur du contrat : corps conforme `{ code, message }` |
| ENF5 | L'application démarre depuis un clone vierge en trois commandes maximum avec des données de démonstration | Procédure testée sur un poste vierge, documentée dans le `README` |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session ; après, il ne marche plus | Q2 |
| RG2 | Un étudiant ne peut marquer qu'une seule présence par session | Déduit de Q1/Q16 + Annexe B (409 déjà présent) |
| RG3 | Après 5 codes erronés (inconnus ou expirés), un étudiant est bloqué 2 minutes | Q4 |
| RG4 | Un étudiant ne peut pas relire son propre exercice | Q5 |
| RG5 | Un seul relecteur par exercice | Q6 |
| RG6 | Le relecteur est choisi au hasard parmi les étudiants présents à la session, à l'exclusion de l'auteur | Q7 + RG4 |
| RG7 | La note est un entier entre 0 et 20 | Q9 |
| RG8 | Une présence ne peut pas être marquée après la fin (clôture) de la session | Q3 |
| RG9 | Un exercice peut être déposé jusqu'à la clôture de la session, même après l'expiration du code | Q12 |
| RG10 | L'auteur ne voit jamais le nom du relecteur, seulement la note et le commentaire | Q8 |
| RG11 | Un lien d'exercice peut être remplacé tant que la relecture n'a pas commencé | Q13 |
| RG12 | Une présence ajoutée à la main par le formateur est marquée `source=FORMATEUR` (« ajouté par le formateur ») | Q14 |
| RG13 | Le relecteur peut corriger sa note tant que la session n'est pas clôturée ; après clôture, plus rien n'est modifiable | Q10, tranché contre Q15 (section 7, C1) |
| RG14 | Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur | Q11 |
| RG15 | La moyenne affichée est la moyenne des notes reçues par l'étudiant, calculée par l'API ; nulle s'il n'a reçu aucune note | Q16 + F3 du sujet |

## 7. Zones d'ombre, hypothèses et contradictions

**Points que la demande ne tranche pas :**

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| H1 — Quand la relecture est-elle assignée ? | Hypothèse (aucune Qx) : Q7 dit seulement « au hasard parmi les présents » | Assignation à chaque dépôt d'exercice : le système choisit un présent au hasard, à l'exclusion de l'auteur | Si un seul étudiant est présent et dépose, il n'y a pas de relecteur possible ; l'exercice reste `EN_ATTENTE` et apparaît dans le tableau (RG14). Cas couvert par une issue dédiée |
| H2 — Le relecteur est-il un acteur distinct ? | Hypothèse | Non : un étudiant assigné. Deux tables d'acteurs seulement | Modèle de données plus simple ; voir D2 |
| H3 — Les étudiants « en liste » sans compte : que saisissent-ils ? | Q1 : l'étudiant choisit son nom dans une liste | Le frontend envoie `etudiantId` ; aucune inscription dans le périmètre | Étudiants et promotions préchargés en base (données de démo) |
| H4 — Un étudiant absent peut-il être relecteur ? | Q7 : « présents à cette session » | Non : seuls les présents (source ETUDIANT ou FORMATEUR) sont éligibles | Une présence ajoutée à la main (RG12) rend éligible |
| H5 — Une présence peut-elle être ajoutée après clôture ? | Q14 dit « oui » sans limite de temps ; Q3/Q12 parlent de clôture | Non : la clôture fige tout, y compris les présences manuelles | Cohérence de RG8 ; tracé en section 7 comme décision à la place du client |
| H6 — Un code erroné puis expiré compte double ? | Hypothèse : Q4 dit « cinq erreurs » sans distinguer | Oui : 5 tentatives de code inconnu **ou** expiré déclenchent le blocage | Compteur par étudiant et par fenêtre de 2 minutes |
| H7 — Le formateur voit-il qui a relu quoi ? | Q16 ne le demande pas ; Q8 interdit de montrer le nom à l'auteur | Le tableau ne montre pas les noms de relecteurs ; le formateur voit seulement les relectures en attente par étudiant | Périmètre volontairement restreint à Q16 |
| H8 — Que voit-on d'une session clôturée ? | Hypothèse | Statut visible (ouverte/clôturée) sur le tableau ; les actions refusées renvoient les erreurs du contrat | Les erreurs de clôture sont documentées au contrat en opérations additionnelles |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| C1 — Q10 (« le relecteur peut corriger sa note tant que le formateur n'a pas clôturé ») vs Q15 (« une fois que le relecteur a validé, c'est fini ») | Q10 : correction possible jusqu'à la clôture de session | Q11 et Q16 décrivent un usage réel et vérifiable (« relectures en attente » dans le tableau), la clôture est l'événement qui fige tout ; Q15 est une intention générale contredite par le besoin exprimé en Q10. C'est la contradiction annoncée par le sujet, tranchée et justifiée ici |
| C2 — Q2 (code expire 15 min) vs Q12 (dépôt possible après la fin de session) | Les deux tiennent : l'expiration du **code** (RG1) ne concerne que la **présence** (RG8) ; le **dépôt** reste ouvert jusqu'à la clôture (RG9) | Ce n'est pas une contradiction si on distingue les deux événements : `expirationAt` (15 min) ≠ clôture. Décision écrite pour éviter que l'implémentation ne fusionne les deux horizons |

## 8. Contraintes techniques

Imposées par le sujet (B1–B6, F1–F3), non négociables :
- **B1** Java 17+, Maven, wrapper `mvnw` commité
- **B2** `api/contrat.yaml` respecté à la lettre (chemins, verbes, codes de statut, format d'erreur)
- **B3** Séparation contrôleur / service / repository ; entités JPA jamais exposées en JSON, passage par des DTO
- **B4** Validation des entrées et gestion centralisée des erreurs (`@RestControllerAdvice`), format `{ code, message }` pour toutes les erreurs
- **B5** Migrations versionnées par Flyway, commitées ; `ddl-auto=update` interdit hors tests
- **B6** Un test unitaire sur une règle métier réelle + un test d'intégration sur un endpoint, exécutables sur poste vierge
- **F1** Frontend déclaré et justifié (voir en-tête) ; build qui passe
- **F2** Trois écrans : formateur, étudiant, relecteur
- **F3** Couche API dédiée côté frontend, états de chargement et d'erreur gérés, aucune règle métier dupliquée (moyenne calculée par l'API)

Contraintes que je m'impose :
- Base de données H2 en fichier pour le développement et les tests (migrations Flyway identiques), PostgreSQL visé pour la démonstration
- Diagrammes en Mermaid texte versionné (`docs/diagrammes/`), pas d'images exportées
- Codes d'erreur métier stables en majuscules (`CODE_EXPIRE`, `DEJA_PRESENT`, …) repris à l'identique dans le contrat, le D3, les issues et les tests
- `.gitignore` Java + Node posé avant le premier commit de code ; aucun fichier généré (`target/`, `node_modules/`, `dist/`)

## 9. Livrables

Étape 1 (le présent travail) :
- `docs/CAHIER_DES_CHARGES.md` — les dix sections, dans cet ordre
- `docs/diagrammes/` — D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 (bonus) états d'un exercice
- Backlog en issues GitHub (reproduit dans `docs/BACKLOG.md` pour la revue locale)
- `api/contrat.yaml` complété des opérations additionnelles

Étapes 2 à 6 (vue d'ensemble) :
- Étape 2 : version v0.1 sur les stories Must, une branche et une PR par ticket
- Étape 3 : traitement de l'enveloppe (bug + changement de besoin), mise à jour du présent document et des diagrammes
- Étape 4 : v1.0, `CHANGELOG.md`, `README` testé depuis un clone vierge, backlog restant trié
- Étape 5 : épreuve Git sur dépôt séparé
- Étape 6 : `SOUMISSION.md` téléversé avant 18h00

## 10. Démarche prévue

Ordre des six étapes tel qu'imposé. À l'étape 2, j'attaque dans cet ordre : présences d'abord (EF1–EF4, car RG1/RG2/RG3 conditionnent le modèle de données et le D3), puis dépôt et relecture (EF5–EF9), puis tableau et clôture (EF10–EF12), les Should (EF6, EF11) en fin de course si le temps le permet. Le contrat d'API est figé avant le premier commit de code. Si je prends du retard, je coupe dans les Should et dans la démonstration, jamais dans les tests B6 ni dans la gestion d'erreurs B4. L'enveloppe de l'étape 3 touchera schéma, contrat et frontend : le schéma sera donc versionné par Flyway dès le premier commit de code pour encaisser le changement à moindre coût, et le présent document sera corrigé dans un commit qui le dit.

**Definition of Done — un ticket est terminé quand :**
- Les critères d'acceptation de l'issue sont vérifiés par un test automatisé ou une vérification manuelle documentée
- Le code est passé en revue dans une PR liée à l'issue, l'issue se ferme par le commit
- La migration associée est commitée si le modèle a changé
- Le cahier des charges et les diagrammes restent cohérents avec ce qui est livré

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 2026-09-25 | Version initiale — étape 1 |
