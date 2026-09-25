package cm.kfokam48.backend.dto;

/** Réponse de PUT /api/exercices/{id}/lien — conforme au contrat api/contrat.yaml. */
public record ExerciceLienRemplaceDto(
        Long id,
        String statut,
        String lien) {
}