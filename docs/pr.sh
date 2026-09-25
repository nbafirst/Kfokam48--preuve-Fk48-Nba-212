#!/usr/bin/env bash
# docs/pr.sh — Ouvre une PR documentée selon docs/GABARIT_PR.md, la merge,
# supprime la branche et vérifie la fermeture automatique de l'issue.
#
# Usage (les \n séparent les lignes à l'intérieur d'un argument) :
#   export GITHUB_TOKEN=ghp_xxx
#   bash docs/pr.sh BRANCHE ISSUE TITRE REFS CRITERES TESTS
#
# Exemple :
#   bash docs/pr.sh "feature/RG3-blocage-cinq-erreurs" 5 \
#     "RG3 — Blocage 2 minutes après 5 codes erronés" \
#     "- **EF3** — Le système bloque un étudiant après 5 codes erronés *(section 4)*\n- **RG3** — Après 5 codes erronés (inconnus ou expirés), un étudiant est bloqué 2 minutes *(section 6, Q4)*\n- **H6** — une erreur = code inconnu OU expiré *(section 7)*" \
#     "- [x] 5 tentatives en erreur déclenchent 400 BLOCAGE_ACTIF\n- [x] Pendant les 2 minutes, même un code valide est refusé\n- [x] Après 2 minutes, la tentative redevient possible" \
#     "| PresenceBlocageTest | seuil RG3, fenêtre glissante, déblocage |\n| PresenceCodeErreursTest | le code expiré compte comme erreur |"

set -euo pipefail

BRANCHE="$1"; ISSUE="$2"; TITRE="$3"; REFS="$4"; CRITERES="$5"; TESTS="$6"

REPO="${GITHUB_REPO:-nbafirst/Kfokam48--preuve-Fk48-Nba-212}"
API="https://api.github.com/repos/$REPO"
: "${GITHUB_TOKEN:?il manque GITHUB_TOKEN : export GITHUB_TOKEN=ghp_xxx avant de relancer}"
export GITHUB_TOKEN

# 1. pousser la branche
git push -u origin "$BRANCHE" 2>&1 | tail -1

# 2. construire le corps de la PR selon le gabarit
BODY=$(ISSUE="$ISSUE" TITRE="$TITRE" REFS="$REFS" CRITERES="$CRITERES" TESTS="$TESTS" BRANCHE="$BRANCHE" \
python3 - <<'PY'
import json, os

def lignes(s):
    return "\n".join(s.split("\\n"))

body = f"""## {os.environ['TITRE']}

### Références au cahier des charges

{lignes(os.environ['REFS'])}

**Issue liée :** Closes #{os.environ['ISSUE']}

### Critères d'acceptation (recopiés de l'issue et vérifiés)

{lignes(os.environ['CRITERES'])}

### Vérification (B6) — tests qui prouvent les critères

| Test | Ce qu'il prouve |
|---|---|
{lignes(os.environ['TESTS'])}

### Contraintes respectées

- **B2** — conformité au contrat `api/contrat.yaml` : chemins, verbes, codes HTTP et format d'erreur `{{ code, message }}`
- **B3** — DTO en entrée/sortie, aucune entité JPA exposée
- **B4** — erreurs centralisées via `@RestControllerAdvice`, aucune stack trace
- **B5** — migration Flyway versionnée, `ddl-auto=validate` (si le modèle change)

*Gabarit : docs/GABARIT_PR.md*
"""
print(json.dumps({"title": os.environ['TITRE'], "body": body,
                  "head": os.environ['BRANCHE'], "base": "main"}))
PY
)

# 3. ouvrir la PR
PR=$(curl -sS -X POST -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github+json" -H "Content-Type: application/json" \
  "$API/pulls" -d "$BODY")

NUM=$(printf '%s' "$PR" | python3 -c 'import sys,json;print(json.load(sys.stdin).get("number","?"))')
echo "PR #$NUM ouverte : $TITRE"

# 4. fusionner
curl -sS -X PUT -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github+json" \
  "$API/pulls/$NUM/merge" -d '{"merge_method":"merge"}' \
  | python3 -c 'import sys,json;d=json.load(sys.stdin);print("merge :",d.get("merged"),d.get("message",""))'

# 5. vérifier la fermeture automatique de l'issue
ETAT=$(curl -sS -H "Authorization: Bearer $GITHUB_TOKEN" "$API/issues/$ISSUE" \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["state"])')
echo "issue #$ISSUE : $ETAT"

# 6. supprimer la branche distante
curl -sS -X DELETE -H "Authorization: Bearer $GITHUB_TOKEN" \
  "$API/git/refs/heads/$BRANCHE" -o /dev/null -w 'branche distante supprimée : %{http_code}\n'

echo "Pour finir : git checkout main && git pull origin main && git branch -D $BRANCHE"
