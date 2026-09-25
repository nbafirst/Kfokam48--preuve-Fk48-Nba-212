package cm.kfokam48.backend.dto;

/** Réponse de GET /api/etudiants/{id}/resultats — conforme au contrat api/contrat.yaml. */
public record ResultatExerciceDto(
        Long exerciceId,
        String sessionTitre,
        String statut,
        Integer note,
        String commentaire) {
}