package cm.kfokam48.backend.dto;

/** Réponse de POST /api/exercices — conforme au contrat api/contrat.yaml. */
public record ExerciceCreeDto(
        Long id,
        String statut) {
}
