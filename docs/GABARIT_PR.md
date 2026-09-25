# Gabarit de Pull Request — KFOKAM48

> Chaque PR suit ce gabarit. Le script `docs/pr.sh` l'applique automatiquement
> (titre, corps, labels, merge, suppression de branche) à partir de 4 variables :
> `ISSUE`, `TITRE`, `REFS`, `SA`, `CRITERES`, `TESTS`.

---

## `<EFx ou RGx> — <titre-résultat de l'issue>`

### Références au cahier des charges

- **Exigence fonctionnelle :** <EFx> — <texte intégral de l'exigence> *(section 4)*
- **Règles de gestion :** <RGx> — <texte intégral de la règle> *(section 6, source Qx)*
- **Issue liée :** `Closes #<n>`

### Critères d'acceptation (recopiés de l'issue)

- [x] <critère 1, vérifié par le test X>
- [x] <critère 2, vérifié par le test Y>
- [x] <critère 3, vérifié par le test Z>

### Ce que fait la PR

- <changement 1 — fichiers clés>
- <changement 2>
- <migration Flyway Vn si le modèle change — cohérence avec D2>

### Vérification (B6)

| Test | Ce qu'il prouve |
|---|---|
| `<NomDeClasseTest>` | <règle/contrat prouvé> |
| `...` | ... |

### Contraintes respectées

- **B2** — conformité au contrat `api/contrat.yaml` : <opérations/codes concernés>
- **B3** — DTO en entrée/sortie, aucune entité JPA exposée
- **B4** — erreurs au format `{ code, message }` via `@RestControllerAdvice`
- **B5** — migration versionnée V<n>, `ddl-auto=validate`

### Hors périmètre (tickets séparés)

- <issue #n — pour quoi on ne l'anticipe pas ici>
