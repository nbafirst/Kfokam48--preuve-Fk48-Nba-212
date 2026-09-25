# D1 — Cas d'utilisation

Acteurs tels que définis en section 2 du cahier des charges : le **Relecteur** n'est pas un acteur distinct (hypothèse H2), c'est un étudiant assigné. Les renvois `EFx`/`RGx` correspondent au cahier des charges.

```mermaid
graph LR
    Et((Étudiant))
    Form((Formateur))

    Et --> UC1["Marquer sa présence<br/>avec le code — EF2, RG1, RG2"]
    Et --> UC2["Déposer le lien de<br/>son exercice — EF5, RG9"]
    Et --> UC3["Remplacer son lien tant que<br/>la relecture n'a pas commencé — EF6, RG11"]
    Et --> UC4["Relire l'exercice d'un pair :<br/>note et commentaire — EF8, RG4, RG7"]
    Et --> UC5["Corriger sa note tant que la<br/>session n'est pas clôturée — EF9, RG13"]
    Et --> UC6["Voir sa note et son commentaire<br/>sans le nom du relecteur — EF11, RG10"]

    Form --> UC7["Ouvrir une session<br/>et obtenir un code — EF1, RG1"]
    Form --> UC8["Consulter le tableau :<br/>présences, dépôts, moyenne,<br/>relectures en attente — EF10, RG15"]
    Form --> UC9["Ajouter une présence à la main<br/>« ajouté par le formateur » — EF4, RG12"]
    Form --> UC10["Clôturer la session — EF12, RG8"]
```

Lecture : le relecteur est un **étudiant dans l'état assigné** (hypothèse H2, section 7) — il n'apparaît donc pas comme un acteur séparé. Le formateur ne marque jamais sa propre présence et ne modifie jamais une note (section 2).
