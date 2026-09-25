# Backlog — Étape 1 (à reproduire en issues GitHub)

Format de chaque issue : **titre-résultat** (pas une tâche technique), **règles et exigences citées en texte intégral** (pas seulement leur numéro), critères d'acceptation vérifiables, priorité `Must`/`Should`/`Could`, jalon cible.

Sources : `docs/CAHIER_DES_CHARGES.md` (sections 4, 5, 6, 7) et `api/contrat.yaml` (v1.1).

---

## Rappel des règles et exigences citées dans ce backlog

**Exigences fonctionnelles (section 4 du cahier des charges) :**
- **EF1** — Le formateur ouvre une session et obtient un code de présence
- **EF2** — L'étudiant marque sa présence avec le code
- **EF3** — Le système bloque un étudiant après 5 codes erronés
- **EF4** — Le formateur ajoute une présence à la main
- **EF5** — L'étudiant dépose le lien de son exercice
- **EF6** — L'étudiant remplace son lien tant que personne n'a commencé à le relire
- **EF7** — Le système assigne un relecteur au hasard parmi les étudiants présents à la session
- **EF8** — Le relecteur rend une note entière sur 20 et un commentaire
- **EF9** — Le relecteur corrige sa note tant que la session n'est pas clôturée
- **EF10** — Le formateur consulte le tableau récapitulatif
- **EF11** — L'étudiant consulte la note et le commentaire reçus
- **EF12** — Le formateur clôture la session

**Règles de gestion (section 6 du cahier des charges) :**
- **RG1** — Un code de présence expire 15 minutes après l'ouverture de la session ; après, il ne marche plus *(Q2)*
- **RG2** — Un étudiant ne peut marquer qu'une seule présence par session
- **RG3** — Après 5 codes erronés (inconnus ou expirés), un étudiant est bloqué 2 minutes *(Q4)*
- **RG4** — Un étudiant ne peut pas relire son propre exercice *(Q5)*
- **RG5** — Un seul relecteur par exercice *(Q6)*
- **RG6** — Le relecteur est choisi au hasard parmi les étudiants présents à la session, à l'exclusion de l'auteur *(Q7 + RG4)*
- **RG7** — La note est un entier entre 0 et 20 *(Q9)*
- **RG8** — Une présence ne peut pas être marquée après la clôture de la session *(Q3)*
- **RG9** — Un exercice peut être déposé jusqu'à la clôture de la session, même après l'expiration du code *(Q12)*
- **RG10** — L'auteur ne voit jamais le nom du relecteur, seulement la note et le commentaire *(Q8)*
- **RG11** — Un lien d'exercice peut être remplacé tant que la relecture n'a pas commencé *(Q13)*
- **RG12** — Une présence ajoutée à la main par le formateur est marquée `source=FORMATEUR` (« ajouté par le formateur ») *(Q14)*
- **RG13** — Le relecteur peut corriger sa note tant que la session n'est pas clôturée ; après clôture, plus rien n'est modifiable *(Q10, tranché contre Q15 — section 7, C1)*
- **RG14** — Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur *(Q11)*
- **RG15** — La moyenne affichée est la moyenne des notes reçues par l'étudiant, calculée par l'API ; nulle s'il n'a reçu aucune note *(Q16 + F3)*

**Exigences non fonctionnelles (section 5) :**
- **ENF1** — Le marquage de présence est utilisable sur un téléphone (360 px) : un champ, un bouton
- **ENF2** — `POST /api/presences` répond en moins de 500 ms pour 60 étudiants validant dans la même minute
- **ENF3** — L'unicité de présence tient sous requêtes concurrentes (contrainte SQL, pas seulement le code)
- **ENF4** — Le format d'erreur imposé `{ code, message }` s'applique à toutes les erreurs, sans exception
- **ENF5** — Démarrage depuis un clone vierge en trois commandes maximum, avec données de démonstration

---

## Issues Must — jalon `v0.1`

### #1 — Le formateur ouvre une session et obtient un code de présence

- **Règles et exigences concernées :**
  - **EF1** — Le formateur ouvre une session et obtient un code de présence
  - **RG1** — Un code de présence expire 15 minutes après l'ouverture de la session ; après, il ne marche plus *(Q2)*
- **Contexte :** c'est la première brique : sans session ouverte, ni présence ni dépôt. L'expiration conditionne tout le flux de présence (issues #2, #4).
- **Critères d'acceptation :**
  - [ ] `POST /api/sessions { titre, promotionId }` renvoie `201 { id, code, ouvertureAt, expirationAt }`
  - [ ] `expirationAt = ouvertureAt + 15 min` exactement (RG1)
  - [ ] Un champ manquant renvoie `400 { code, message }` au format imposé (ENF4)
- **Endpoint du contrat :** `POST /api/sessions` — **Priorité :** Must — **Jalon :** v0.1

### #2 — Un étudiant marque sa présence avec un code valide

- **Règles et exigences concernées :**
  - **EF2** — L'étudiant marque sa présence avec le code
  - **RG2** — Un étudiant ne peut marquer qu'une seule présence par session
- **Contexte :** le cas nominal du flux « présence ». La présence est enregistrée avec `source=ETUDIANT` (le champ `source` distingue la présence ajoutée par le formateur, issue #6).
- **Critères d'acceptation :**
  - [ ] `POST /api/presences { code, etudiantId }` renvoie `201 { id, sessionId, etudiantId, source: "ETUDIANT" }`
  - [ ] La présence apparaît dans le tableau du formateur (issue #10)
- **Endpoint du contrat :** `POST /api/presences` — **Priorité :** Must — **Jalon :** v0.1

### #3 — Un étudiant ne peut pas marquer deux fois sa présence sur la même session

- **Règles et exigences concernées :**
  - **RG2** — Un étudiant ne peut marquer qu'une seule présence par session
  - **ENF3** — L'unicité de présence tient sous requêtes concurrentes (contrainte SQL, pas seulement le code)
- **Contexte :** l'unicité est tenue par une contrainte SQL `(session_id, etudiant_id)` sur la table `presence` (voir D2), pas seulement par un test applicatif — deux requêtes simultanées ne doivent jamais créer de doublon.
- **Critères d'acceptation :**
  - [ ] Un deuxième `POST /api/presences` avec le même étudiant et la même session renvoie `409 { code: "DEJA_PRESENT" }`, sans créer de doublon en base
  - [ ] Deux requêtes concurrentes donnent un seul enregistrement
  - [ ] Le premier enregistrement reste inchangé
- **Endpoint du contrat :** `POST /api/presences` — **Priorité :** Must — **Jalon :** v0.1

### #4 — Un code expiré ou inconnu est refusé avec le bon code d'erreur

- **Règles et exigences concernées :**
  - **RG1** — Un code de présence expire 15 minutes après l'ouverture de la session ; après, il ne marche plus *(Q2)*
  - **ENF4** — Le format d'erreur imposé `{ code, message }` s'applique à toutes les erreurs, sans exception
- **Contexte :** deux erreurs distinctes du contrat : le code n'existe pas (`400 CODE_INCONNU`) et le code existait mais a expiré (`410 CODE_EXPIRE`). Le D3 (séquence présence) documente ces deux branches — les codes HTTP doivent être identiques partout.
- **Critères d'acceptation :**
  - [ ] Code inexistant → `400 { code: "CODE_INCONNU" }`
  - [ ] Code dont la session a dépassé `expirationAt` → `410 { code: "CODE_EXPIRE" }`
  - [ ] Les deux corps respectent le format d'erreur imposé
- **Endpoint du contrat :** `POST /api/presences` — **Priorité :** Must — **Jalon :** v0.1

### #5 — Un étudiant est bloqué 2 minutes après 5 codes erronés

- **Règles et exigences concernées :**
  - **EF3** — Le système bloque un étudiant après 5 codes erronés
  - **RG3** — Après 5 codes erronés (inconnus ou expirés), un étudiant est bloqué 2 minutes *(Q4)*
- **Contexte :** le client veut empêcher que les étudiants devinent les codes entre eux. Une erreur de code = inconnu **ou** expiré (H6, section 7) : les deux comptent dans le même compteur.
- **Critères d'acceptation :**
  - [ ] 5 tentatives en erreur (code inconnu ou expiré) déclenchent un refus avec `400 { code: "BLOCAGE_ACTIF" }`
  - [ ] Pendant les 2 minutes, même un code valide est refusé
  - [ ] Après 2 minutes, la tentative redevient possible
- **Endpoint du contrat :** `POST /api/presences` — **Priorité :** Must — **Jalon :** v0.1

### #6 — Le formateur ajoute une présence marquée « ajouté par le formateur »

- **Règles et exigences concernées :**
  - **EF4** — Le formateur ajoute une présence à la main
  - **RG12** — Une présence ajoutée à la main par le formateur est marquée `source=FORMATEUR` (« ajouté par le formateur ») *(Q14)*
  - **RG8** — Une présence ne peut pas être marquée après la clôture de la session *(Q3)*
- **Contexte :** le client ajoute à la main la présence d'un étudiant dont le téléphone a un souci. Ça doit se voir dans le tableau. Après clôture, refus (H5, section 7).
- **Critères d'acceptation :**
  - [ ] La présence est créée avec `source: "FORMATEUR"`
  - [ ] Le tableau distingue visuellement la présence ajoutée à la main
  - [ ] Après clôture de la session, la demande renvoie `409 { code: "SESSION_CLOTUREE" }`
- **Endpoint du contrat :** `POST /api/sessions/{id}/presences` — **Priorité :** Must — **Jalon :** v0.1

### #7 — Un étudiant dépose le lien de son exercice

- **Règles et exigences concernées :**
  - **EF5** — L'étudiant dépose le lien de son exercice
  - **RG9** — Un exercice peut être déposé jusqu'à la clôture de la session, même après l'expiration du code *(Q12)*
- **Contexte :** attention à la distinction C2 (section 7) : l'**expiration du code** (RG1) ne concerne que la **présence** ; le **dépôt** reste ouvert jusqu'à la **clôture** de la session. Un étudiant présent peut donc déposer longtemps après les 15 minutes.
- **Critères d'acceptation :**
  - [ ] `POST /api/exercices { sessionId, etudiantId, lien }` renvoie `201 { id, statut: "EN_ATTENTE" }`
  - [ ] Un second dépôt du même étudiant sur la même session renvoie `409 { code: "EXERCICE_DEJA_DEPOSE" }`
  - [ ] Un lien non URI renvoie `400 { code: "LIEN_INVALIDE" }`
  - [ ] Le dépôt reste possible après l'expiration du code, tant que la session n'est pas clôturée (RG9)
- **Endpoint du contrat :** `POST /api/exercices` — **Priorité :** Must — **Jalon :** v0.1

### #8 — Le système assigne un relecteur au hasard parmi les présents

- **Règles et exigences concernées :**
  - **EF7** — Le système assigne un relecteur au hasard parmi les étudiants présents à la session
  - **RG4** — Un étudiant ne peut pas relire son propre exercice *(Q5)*
  - **RG5** — Un seul relecteur par exercice *(Q6)*
  - **RG6** — Le relecteur est choisi au hasard parmi les étudiants présents à la session, à l'exclusion de l'auteur *(Q7 + RG4)*
  - **RG14** — Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur *(Q11)*
- **Contexte :** l'assignation se fait **au dépôt** (H1, section 7). Si l'auteur est le seul présent, aucun relecteur n'est désignable : l'exercice reste `EN_ATTENTE` et compte dans les relectures en attente du tableau.
- **Critères d'acceptation :**
  - [ ] Au dépôt, un relecteur est choisi parmi les présents de la session, jamais l'auteur
  - [ ] L'exercice passe en `ASSIGNE` ; un seul relecteur par exercice (contrainte SQL sur `relecture.exercice_id`, voir D2)
  - [ ] S'il n'y a aucun autre présent, l'exercice reste `EN_ATTENTE` et compte dans « relectures en attente » du tableau
- **Endpoint du contrat :** `POST /api/exercices` (effet de bord de l'assignation) — **Priorité :** Must — **Jalon :** v0.1

### #9 — Un relecteur rend une note entière sur 20 et un commentaire

- **Règles et exigences concernées :**
  - **EF8** — Le relecteur rend une note entière sur 20 et un commentaire
  - **RG4** — Un étudiant ne peut pas relire son propre exercice *(Q5)*
  - **RG7** — La note est un entier entre 0 et 20 *(Q9)*
  - **RG10** — L'auteur ne voit jamais le nom du relecteur, seulement la note et le commentaire *(Q8)*
  - **RG5** — Un seul relecteur par exercice *(Q6)*
- **Contexte :** trois erreurs métier prévues par le contrat : note invalide (`400`), auto-relecture (`403`), relecture déjà rendue (`409`). Le nom du relecteur ne sort jamais dans les DTO exposés à l'auteur.
- **Critères d'acceptation :**
  - [ ] `POST /api/relectures/{id} { note, commentaire }` renvoie `200` et l'exercice passe en `RELU`
  - [ ] Note non entière ou hors 0–20 → `400 { code: "NOTE_INVALIDE" }`
  - [ ] Relecture de son propre exercice → `403 { code: "AUTO_RELECTURE" }`
  - [ ] Deuxième envoi → `409 { code: "RELECTURE_DEJA_RENDUE" }`
  - [ ] L'auteur voit la note et le commentaire sans le nom du relecteur
- **Endpoint du contrat :** `POST /api/relectures/{id}` — **Priorité :** Must — **Jalon :** v0.1

### #10 — Le formateur voit le tableau récapitulatif

- **Règles et exigences concernées :**
  - **EF10** — Le formateur consulte le tableau récapitulatif
  - **RG15** — La moyenne affichée est la moyenne des notes reçues par l'étudiant, calculée par l'API ; nulle s'il n'a reçu aucune note *(Q16 + F3)*
  - **RG14** — Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur *(Q11)*
  - **ENF2** — `POST /api/presences` répond en moins de 500 ms pour 60 étudiants validant dans la même minute
- **Contexte :** ce que le client a demandé de voir (Q16) : par étudiant — présences, exercices déposés, moyenne des notes reçues, relectures en attente. La moyenne est calculée **par l'API**, jamais recalculée côté frontend (F3 du sujet).
- **Critères d'acceptation :**
  - [ ] `GET /api/tableau?promotionId=` renvoie `200 [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]`
  - [ ] Promotion inconnue → `404 { code: "PROMOTION_INCONNUE" }`
  - [ ] `moyenne` est `null` quand l'étudiant n'a reçu aucune note
  - [ ] Réponse en moins de 2 s pour 60 étudiants (ENF2)
- **Endpoint du contrat :** `GET /api/tableau` — **Priorité :** Must — **Jalon :** v0.1

### #11 — Le formateur clôture la session et tout est figé

- **Règles et exigences concernées :**
  - **EF12** — Le formateur clôture la session
  - **RG8** — Une présence ne peut pas être marquée après la clôture de la session *(Q3)*
  - **RG13** — Le relecteur peut corriger sa note tant que la session n'est pas clôturée ; après clôture, plus rien n'est modifiable *(Q10, tranché contre Q15 — section 7, C1)*
  - **RG9** — Un exercice peut être déposé jusqu'à la clôture de la session, même après l'expiration du code *(Q12)*
  - **RG14** — Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur *(Q11)*
- **Contexte :** la clôture est l'événement qui fige tout — c'est aussi ce qui justifie le choix C1 (Q10 retenu contre Q15 : la correction est possible **jusqu'à** la clôture). Les relectures jamais rendues restent visibles « en attente » dans le tableau.
- **Critères d'acceptation :**
  - [ ] `POST /api/sessions/{id}/cloture` renvoie `200 { id, clotureAt }` ; un deuxième appel renvoie `409 { code: "SESSION_DEJA_CLOTUREE" }`
  - [ ] Après clôture : présence, dépôt, remplacement de lien et correction de note renvoient `409 { code: "SESSION_CLOTUREE" }`
  - [ ] Les relectures jamais rendues restent visibles « en attente » dans le tableau
- **Endpoint du contrat :** `POST /api/sessions/{id}/cloture` — **Priorité :** Must — **Jalon :** v0.1

---

## Issues Should — jalon `v1.0`

### #12 — Un étudiant remplace son lien tant que personne n'a commencé à le relire

- **Règles et exigences concernées :**
  - **EF6** — L'étudiant remplace son lien tant que personne n'a commencé à le relire
  - **RG11** — Un lien d'exercice peut être remplacé tant que la relecture n'a pas commencé *(Q13)*
- **Contexte :** « commencé à relire » = relecteur désigné (état `ASSIGNE`, D4). Dès `ASSIGNE`, le remplacement est verrouillé (`LIEN_VERROUILLE`).
- **Critères d'acceptation :**
  - [ ] Remplacement accepté tant que la relecture n'est pas rendue (exercice `EN_ATTENTE`)
  - [ ] Refusé dès que la relecture est rendue, avec `409 { code: "LIEN_VERROUILLE" }`
- **Endpoint du contrat :** `PUT /api/exercices/{id}/lien` — **Priorité :** Should — **Jalon :** v1.0

### #13 — Un relecteur corrige sa note tant que la session n'est pas clôturée

- **Règles et exigences concernées :**
  - **EF9** — Le relecteur corrige sa note tant que la session n'est pas clôturée
  - **RG13** — Le relecteur peut corriger sa note tant que la session n'est pas clôturée ; après clôture, plus rien n'est modifiable *(Q10, tranché contre Q15 — section 7, C1)*
- **Contexte :** **c'est l'issue de la contradiction tranchée** : le client dit à la fois « il peut corriger » (Q10) et « une fois validé, c'est fini » (Q15). Décision écrite en section 7 : **Q10 retenu**, parce que Q11 et Q16 décrivent un usage réel alors que Q15 est une intention générale. La clôture de session (issue #11) est l'événement qui fige la note.
- **Critères d'acceptation :**
  - [ ] Un nouvel envoi du relecteur met la note à jour avant clôture
  - [ ] Après clôture, la correction est refusée avec `409 { code: "SESSION_CLOTUREE" }`
- **Endpoint du contrat :** `POST /api/relectures/{id}/correction` — **Priorité :** Should — **Jalon :** v1.0

### #14 — L'étudiant consulte sa note et son commentaire sans voir le nom du relecteur

- **Règles et exigences concernées :**
  - **EF11** — L'étudiant consulte la note et le commentaire reçus
  - **RG10** — L'auteur ne voit jamais le nom du relecteur, seulement la note et le commentaire *(Q8)*
- **Contexte :** l'anonymat du relecteur est le principe même du dispositif (Q8). L'absence de nom doit être garantie côté **DTO**, pas seulement cachée dans l'interface.
- **Critères d'acceptation :**
  - [ ] L'écran étudiant affiche note et commentaire de chaque exercice relu
  - [ ] Aucune réponse API n'expose le nom du relecteur à l'auteur (vérifié dans les DTO)
- **Endpoint du contrat :** `GET /api/etudiants/{id}/resultats` — **Priorité :** Should — **Jalon :** v1.0

### #15 — L'écran étudiant est utilisable sur un téléphone

- **Règles et exigences concernées :**
  - **ENF1** — Le marquage de présence est utilisable sur un téléphone (360 px) : un champ, un bouton
- **Contexte :** le code de présence sera saisi sur téléphone en fin de séance (H du contexte, section 5) : formulaire court, gros bouton, aucun scroll horizontal.
- **Critères d'acceptation :**
  - [ ] Sur 360 px de large : formulaire de présence à un champ + bouton, aucun scroll horizontal
- **Endpoint du contrat :** (écran frontend) — **Priorité :** Should — **Jalon :** v1.0

---

## Issue Could — jalon `v1.0`

### #16 — Écran de clôture et statut de session pour le formateur

- **Règles et exigences concernées :**
  - **EF12** — Le formateur clôture la session
  - Hypothèse **H8** — Le formateur voit le statut (ouverte/clôturée) et déclenche la clôture depuis son écran
- **Contexte :** confort d'interface pour l'exigence déjà couverte côté API par l'issue #11.
- **Critères d'acceptation :**
  - [ ] Le formateur voit le statut (ouverte/clôturée) et déclenche la clôture depuis son écran
- **Endpoint du contrat :** `POST /api/sessions/{id}/cloture` — **Priorité :** Could — **Jalon :** v1.0

---

## Répartition

- **Must (v0.1) :** #1 → #11 (11 issues, couvrent EF1–EF12)
- **Should (v1.0) :** #12 → #15
- **Could :** #16

## Checklist de cohérence

- [x] Chaque exigence fonctionnelle (EF1 à EF12) est citée **en texte intégral** par au moins une issue
- [x] Chaque règle de gestion citée est recopiée du cahier des charges, avec sa source (`Qx`)
- [x] Aucune issue ne référence un `EFx`, `RGx` ou `ENFx` inexistant (RG1–RG15, ENF1–ENF5)
- [x] Tous les titres décrivent un résultat utilisateur, pas une tâche technique
- [x] Priorités limitées à Must / Should / Could
- [x] Chaque issue pointe vers l'endpoint de `api/contrat.yaml` qui la réalise
