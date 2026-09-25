package cm.kfokam48.backend.dto;

/** Réponse de GET /api/tableau — conforme au contrat api/contrat.yaml. */
public record TableauLigneDto(
        Long etudiantId,
        String nom,
        Integer presences,
        Integer exercicesDeposes,
        Double moyenne,
        Integer relecturesEnAttente) {
}