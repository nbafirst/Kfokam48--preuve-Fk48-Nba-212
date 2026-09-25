#!/usr/bin/env bash
# docs/issues.sh — Crée les 16 issues du backlog sur GitHub (API REST, curl).
#
# PRÉREQUIS
#   1. Un token GitHub ayant le droit de créer des issues sur le dépôt :
#        - token classic (scope repo) :
#          https://github.com/settings/tokens/new?scopes=repo&description=issues%20kfokam48
#        - ou token fine-grained avec « Issues : Read and write » sur le dépôt
#   2. Exporte-le dans le terminal (ne l'écris JAMAIS dans ce fichier) :
#        export GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxx
#   3. Lance :
#        bash docs/issues.sh
#
# À exécuter APRÈS le push du commit [JALON] analyse et AVANT le premier commit de code.

set -euo pipefail

REPO="${GITHUB_REPO:-nbafirst/Kfokam48--preuve-Fk48-Nba-212}"
API="https://api.github.com/repos/$REPO"

: "${GITHUB_TOKEN:?il manque GITHUB_TOKEN : export GITHUB_TOKEN=ghp_xxx avant de relancer}"
export GITHUB_TOKEN

# --- Étiquettes de priorité (créées si absentes, erreurs ignorées) ---
ensure_label() {
  curl -sS -o /dev/null -X POST \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github+json" \
    -H "Content-Type: application/json" \
    "$API/labels" \
    -d "{\"name\":\"$1\",\"color\":\"$2\",\"description\":\"$3\"}" || true
}
ensure_label "Must"   "D73A4A" "v0.1 - indispensable"
ensure_label "Should" "E4B429" "v1.0 - important"
ensure_label "Could"  "0E8A16" "si le temps le permet"

CREATED=0

create_issue() {
  local title="$1" labels="$2"
  local tmpdir http num
  tmpdir=$(mktemp -d)

  # le corps de l'issue arrive sur l'entrée standard
  cat > "$tmpdir/body.md"

  # construit le JSON de la requête
  TITLE="$title" LABELS="$labels" BODY_FILE="$tmpdir/body.md" \
    python3 - > "$tmpdir/payload.json" <<'PY'
import json, os
body = open(os.environ["BODY_FILE"], encoding="utf-8").read()
print(json.dumps({
    "title": os.environ["TITLE"],
    "body": body,
    "labels": [l.strip() for l in os.environ["LABELS"].split(",") if l.strip()],
}))
PY

  # envoie la requête ; le code HTTP seul va dans $http, la réponse dans un fichier
  http=$(curl -sS -o "$tmpdir/resp.json" -w '%{http_code}' -X POST \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github+json" \
    -H "Content-Type: application/json" \
    "$API/issues" \
    --data-binary @"$tmpdir/payload.json")

  num=$(python3 -c 'import sys,json;print(json.load(open(sys.argv[1])).get("number","?"))' "$tmpdir/resp.json" 2>/dev/null || echo "?")

  if [ "$http" = "201" ]; then
    echo "OK  #$num  $title"
    CREATED=$((CREATED+1))
  elif [ "$http" = "422" ]; then
    echo "ATTENTION (deja existante ?)  $title"
  else
    echo "ERREUR HTTP $http  $title"
    cat "$tmpdir/resp.json"
    rm -rf "$tmpdir"
    exit 1
  fi
  rm -rf "$tmpdir"
  sleep 1   # respecte la limite secondaire de l'API
}

echo "Création des issues sur $REPO ..."
echo

# ============================ MUST — jalon v0.1 ============================

create_issue "Le formateur ouvre une session et obtient un code de présence" "Must" <<'EOF'
**Exigence et règles concernées (citées du cahier des charges, sections 4 et 6) :**

- **EF1** — Le formateur ouvre une session et obtient un code de présence
- **RG1** — Un code de présence expire 15 minutes après l'ouverture de la session ; après, il ne marche plus *(source : Q2)*

**Critères d'acceptation :**
- [ ] `POST /api/sessions { titre, promotionId }` renvoie `201 { id, code, ouvertureAt, expirationAt }`
- [ ] `expirationAt = ouvertureAt + 15 min` exactement (RG1)
- [ ] Un champ manquant renvoie `400 { code, message }` au format imposé (ENF4)

**Endpoint :** `POST /api/sessions` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Un étudiant marque sa présence avec un code valide" "Must" <<'EOF'
**Exigence et règles concernées :**

- **EF2** — L'étudiant marque sa présence avec le code
- **RG2** — Un étudiant ne peut marquer qu'une seule présence par session

**Critères d'acceptation :**
- [ ] `POST /api/presences { code, etudiantId }` renvoie `201 { id, sessionId, etudiantId, source: "ETUDIANT" }`
- [ ] La présence apparaît dans le tableau du formateur

**Endpoint :** `POST /api/presences` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Un étudiant ne peut pas marquer deux fois sa présence sur la même session" "Must" <<'EOF'
**Règles concernées :**

- **RG2** — Un étudiant ne peut marquer qu'une seule présence par session
- **ENF3** — L'unicité de présence tient sous requêtes concurrentes (contrainte SQL, pas seulement le code)

**Contexte :** l'unicité est tenue par une contrainte SQL `(session_id, etudiant_id)` sur la table `presence` (voir diagramme D2), pas seulement par un test applicatif.

**Critères d'acceptation :**
- [ ] Un deuxième `POST /api/presences` avec le même étudiant et la même session renvoie `409 { code: "DEJA_PRESENT" }`, sans créer de doublon en base
- [ ] Deux requêtes concurrentes donnent un seul enregistrement
- [ ] Le premier enregistrement reste inchangé

**Endpoint :** `POST /api/presences` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Un code expiré ou inconnu est refusé avec le bon code d'erreur" "Must" <<'EOF'
**Règles concernées :**

- **RG1** — Un code de présence expire 15 minutes après l'ouverture de la session ; après, il ne marche plus *(source : Q2)*
- **ENF4** — Le format d'erreur imposé `{ code, message }` s'applique à toutes les erreurs, sans exception

**Contexte :** deux erreurs distinctes du contrat : code inexistant (`400 CODE_INCONNU`) et code existant mais expiré (`410 CODE_EXPIRE`). Le diagramme D3 documente ces deux branches — mêmes codes HTTP partout.

**Critères d'acceptation :**
- [ ] Code inexistant → `400 { code: "CODE_INCONNU" }`
- [ ] Code dont la session a dépassé `expirationAt` → `410 { code: "CODE_EXPIRE" }`
- [ ] Les deux corps respectent le format d'erreur imposé

**Endpoint :** `POST /api/presences` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Un étudiant est bloqué 2 minutes après 5 codes erronés" "Must" <<'EOF'
**Exigence et règle concernées :**

- **EF3** — Le système bloque un étudiant après 5 codes erronés
- **RG3** — Après 5 codes erronés (inconnus ou expirés), un étudiant est bloqué 2 minutes *(source : Q4)*

**Contexte :** le client veut empêcher les étudiants de deviner les codes entre eux. Une « erreur » = code inconnu **ou** expiré (hypothèse H6, section 7) : les deux comptent dans le même compteur.

**Critères d'acceptation :**
- [ ] 5 tentatives en erreur (code inconnu ou expiré) déclenchent un refus avec `400 { code: "BLOCAGE_ACTIF" }`
- [ ] Pendant les 2 minutes, même un code valide est refusé
- [ ] Après 2 minutes, la tentative redevient possible

**Endpoint :** `POST /api/presences` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Le formateur ajoute une présence marquée « ajouté par le formateur »" "Must" <<'EOF'
**Exigence et règles concernées :**

- **EF4** — Le formateur ajoute une présence à la main
- **RG12** — Une présence ajoutée à la main par le formateur est marquée `source=FORMATEUR` (« ajouté par le formateur ») *(source : Q14)*
- **RG8** — Une présence ne peut pas être marquée après la clôture de la session *(source : Q3)*

**Contexte :** le client ajoute à la main la présence d'un étudiant dont le téléphone a un souci — et il faut que ça se voie. Après clôture, refus (hypothèse H5, section 7).

**Critères d'acceptation :**
- [ ] La présence est créée avec `source: "FORMATEUR"`
- [ ] Le tableau distingue visuellement la présence ajoutée à la main
- [ ] Après clôture de la session, la demande renvoie `409 { code: "SESSION_CLOTUREE" }`

**Endpoint :** `POST /api/sessions/{id}/presences` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Un étudiant dépose le lien de son exercice" "Must" <<'EOF'
**Exigence et règle concernées :**

- **EF5** — L'étudiant dépose le lien de son exercice
- **RG9** — Un exercice peut être déposé jusqu'à la clôture de la session, même après l'expiration du code *(source : Q12)*

**Contexte :** distinction C2 (section 7) : l'**expiration du code** (RG1) ne concerne que la **présence** ; le **dépôt** reste ouvert jusqu'à la **clôture** de la session. Un étudiant présent peut déposer bien après les 15 minutes.

**Critères d'acceptation :**
- [ ] `POST /api/exercices { sessionId, etudiantId, lien }` renvoie `201 { id, statut: "EN_ATTENTE" }`
- [ ] Un second dépôt du même étudiant sur la même session renvoie `409 { code: "EXERCICE_DEJA_DEPOSE" }`
- [ ] Un lien non URI renvoie `400 { code: "LIEN_INVALIDE" }`
- [ ] Le dépôt reste possible après l'expiration du code, tant que la session n'est pas clôturée (RG9)

**Endpoint :** `POST /api/exercices` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Le système assigne un relecteur au hasard parmi les présents" "Must" <<'EOF'
**Exigence et règles concernées :**

- **EF7** — Le système assigne un relecteur au hasard parmi les étudiants présents à la session
- **RG4** — Un étudiant ne peut pas relire son propre exercice *(source : Q5)*
- **RG5** — Un seul relecteur par exercice *(source : Q6)*
- **RG6** — Le relecteur est choisi au hasard parmi les étudiants présents à la session, à l'exclusion de l'auteur *(source : Q7 + RG4)*
- **RG14** — Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur *(source : Q11)*

**Contexte :** l'assignation se fait **au dépôt** (hypothèse H1, section 7). Si l'auteur est le seul présent, aucun relecteur n'est désignable : l'exercice reste `EN_ATTENTE` et compte dans les relectures en attente du tableau.

**Critères d'acceptation :**
- [ ] Au dépôt, un relecteur est choisi parmi les présents de la session, jamais l'auteur
- [ ] L'exercice passe en `ASSIGNE` ; un seul relecteur par exercice (contrainte SQL sur `relecture.exercice_id`, voir D2)
- [ ] S'il n'y a aucun autre présent, l'exercice reste `EN_ATTENTE` et compte dans « relectures en attente » du tableau

**Endpoint :** `POST /api/exercices` (effet de bord de l'assignation) · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Un relecteur rend une note entière sur 20 et un commentaire" "Must" <<'EOF'
**Exigence et règles concernées :**

- **EF8** — Le relecteur rend une note entière sur 20 et un commentaire
- **RG4** — Un étudiant ne peut pas relire son propre exercice *(source : Q5)*
- **RG7** — La note est un entier entre 0 et 20 *(source : Q9)*
- **RG10** — L'auteur ne voit jamais le nom du relecteur, seulement la note et le commentaire *(source : Q8)*
- **RG5** — Un seul relecteur par exercice *(source : Q6)*

**Contexte :** trois erreurs métier prévues par le contrat : note invalide (`400`), auto-relecture (`403`), relecture déjà rendue (`409`). Le nom du relecteur ne sort jamais dans les DTO exposés à l'auteur.

**Critères d'acceptation :**
- [ ] `POST /api/relectures/{id} { note, commentaire }` renvoie `200` et l'exercice passe en `RELU`
- [ ] Note non entière ou hors 0–20 → `400 { code: "NOTE_INVALIDE" }`
- [ ] Relecture de son propre exercice → `403 { code: "AUTO_RELECTURE" }`
- [ ] Deuxième envoi → `409 { code: "RELECTURE_DEJA_RENDUE" }`
- [ ] L'auteur voit la note et le commentaire sans le nom du relecteur

**Endpoint :** `POST /api/relectures/{id}` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Le formateur voit le tableau récapitulatif" "Must" <<'EOF'
**Exigence, règle et exigence non fonctionnelle concernées :**

- **EF10** — Le formateur consulte le tableau récapitulatif
- **RG15** — La moyenne affichée est la moyenne des notes reçues par l'étudiant, calculée par l'API ; nulle s'il n'a reçu aucune note *(source : Q16 + F3 du sujet)*
- **RG14** — Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur *(source : Q11)*
- **ENF2** — `POST /api/presences` répond en moins de 500 ms pour 60 étudiants validant dans la même minute

**Contexte :** ce que le client a demandé de voir (Q16), par étudiant : présences, exercices déposés, moyenne des notes reçues, relectures en attente. La moyenne est calculée **par l'API**, jamais recalculée côté frontend (F3).

**Critères d'acceptation :**
- [ ] `GET /api/tableau?promotionId=` renvoie `200 [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]`
- [ ] Promotion inconnue → `404 { code: "PROMOTION_INCONNUE" }`
- [ ] `moyenne` est `null` quand l'étudiant n'a reçu aucune note
- [ ] Réponse en moins de 2 s pour 60 étudiants (ENF2)

**Endpoint :** `GET /api/tableau` · **Priorité :** Must · **Jalon :** v0.1
EOF

create_issue "Le formateur clôture la session et tout est figé" "Must" <<'EOF'
**Exigence et règles concernées :**

- **EF12** — Le formateur clôture la session
- **RG8** — Une présence ne peut pas être marquée après la clôture de la session *(source : Q3)*
- **RG13** — Le relecteur peut corriger sa note tant que la session n'est pas clôturée ; après clôture, plus rien n'est modifiable *(source : Q10, tranché contre Q15 — section 7, C1)*
- **RG9** — Un exercice peut être déposé jusqu'à la clôture de la session, même après l'expiration du code *(source : Q12)*
- **RG14** — Un exercice sans relecture rendue reste « en attente » et apparaît comme tel dans le tableau du formateur *(source : Q11)*

**Contexte :** la clôture est l'événement qui fige tout — c'est aussi ce qui justifie le choix C1 (Q10 retenu contre Q15 : la correction est possible **jusqu'à** la clôture).

**Critères d'acceptation :**
- [ ] `POST /api/sessions/{id}/cloture` renvoie `200 { id, clotureAt }` ; un deuxième appel renvoie `409 { code: "SESSION_DEJA_CLOTUREE" }`
- [ ] Après clôture : présence, dépôt, remplacement de lien et correction de note renvoient `409 { code: "SESSION_CLOTUREE" }`
- [ ] Les relectures jamais rendues restent visibles « en attente » dans le tableau

**Endpoint :** `POST /api/sessions/{id}/cloture` · **Priorité :** Must · **Jalon :** v0.1
EOF

# ============================ SHOULD — jalon v1.0 ============================

create_issue "Un étudiant remplace son lien tant que personne n'a commencé à le relire" "Should" <<'EOF'
**Exigence et règle concernées :**

- **EF6** — L'étudiant remplace son lien tant que personne n'a commencé à le relire
- **RG11** — Un lien d'exercice peut être remplacé tant que la relecture n'a pas commencé *(source : Q13)*

**Contexte :** « commencé à relire » = relecteur désigné (état `ASSIGNE`, diagramme D4). Dès `ASSIGNE`, le remplacement est verrouillé (`LIEN_VERROUILLE`).

**Critères d'acceptation :**
- [ ] Remplacement accepté tant que la relecture n'est pas rendue (exercice `EN_ATTENTE`)
- [ ] Refusé dès que la relecture est rendue, avec `409 { code: "LIEN_VERROUILLE" }`

**Endpoint :** `PUT /api/exercices/{id}/lien` · **Priorité :** Should · **Jalon :** v1.0
EOF

create_issue "Un relecteur corrige sa note tant que la session n'est pas clôturée" "Should" <<'EOF'
**Exigence et règle concernées :**

- **EF9** — Le relecteur corrige sa note tant que la session n'est pas clôturée
- **RG13** — Le relecteur peut corriger sa note tant que la session n'est pas clôturée ; après clôture, plus rien n'est modifiable *(source : Q10, tranché contre Q15 — section 7, C1)*

**Contexte :** c'est l'issue de la **contradiction tranchée** : le client dit à la fois « il peut corriger » (Q10) et « une fois validé, c'est fini » (Q15). Décision écrite en section 7 : **Q10 retenu**, parce que Q11 et Q16 décrivent un usage réel alors que Q15 est une intention générale. La clôture de session (issue « Le formateur clôture la session ») est l'événement qui fige la note.

**Critères d'acceptation :**
- [ ] Un nouvel envoi du relecteur met la note à jour avant clôture
- [ ] Après clôture, la correction est refusée avec `409 { code: "SESSION_CLOTUREE" }`

**Endpoint :** `POST /api/relectures/{id}/correction` · **Priorité :** Should · **Jalon :** v1.0
EOF

create_issue "L'étudiant consulte sa note et son commentaire sans voir le nom du relecteur" "Should" <<'EOF'
**Exigence et règle concernées :**

- **EF11** — L'étudiant consulte la note et le commentaire reçus
- **RG10** — L'auteur ne voit jamais le nom du relecteur, seulement la note et le commentaire *(source : Q8)*

**Contexte :** l'anonymat du relecteur est le principe même du dispositif (Q8). L'absence de nom doit être garantie côté **DTO**, pas seulement cachée dans l'interface.

**Critères d'acceptation :**
- [ ] L'écran étudiant affiche note et commentaire de chaque exercice relu
- [ ] Aucune réponse API n'expose le nom du relecteur à l'auteur (vérifié dans les DTO)

**Endpoint :** `GET /api/etudiants/{id}/resultats` · **Priorité :** Should · **Jalon :** v1.0
EOF

create_issue "L'écran étudiant est utilisable sur un téléphone" "Should" <<'EOF'
**Exigence non fonctionnelle concernée :**

- **ENF1** — Le marquage de présence est utilisable sur un téléphone (360 px) : un champ, un bouton

**Contexte :** le code de présence sera saisi sur téléphone en fin de séance : formulaire court, gros bouton, aucun scroll horizontal.

**Critères d'acceptation :**
- [ ] Sur 360 px de large : formulaire de présence à un champ + bouton, aucun scroll horizontal

**Portée :** écran frontend · **Priorité :** Should · **Jalon :** v1.0
EOF

# ============================ COULD — jalon v1.0 ============================

create_issue "Écran de clôture et statut de session pour le formateur" "Could" <<'EOF'
**Exigence et hypothèse concernées :**

- **EF12** — Le formateur clôture la session
- **H8** — Le formateur voit le statut (ouverte/clôturée) et déclenche la clôture depuis son écran (section 7 du cahier des charges)

**Contexte :** confort d'interface pour l'exigence déjà couverte côté API par l'issue « Le formateur clôture la session et tout est figé ».

**Critères d'acceptation :**
- [ ] Le formateur voit le statut (ouverte/clôturée) et déclenche la clôture depuis son écran

**Endpoint :** `POST /api/sessions/{id}/cloture` · **Priorité :** Could · **Jalon :** v1.0
EOF

echo
echo "Terminé : $CREATED issue(s) créée(s) sur $REPO."
echo "Vérifie : https://github.com/$REPO/issues"
