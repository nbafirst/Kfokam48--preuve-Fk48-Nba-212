package cm.kfokam48.backend.dto;

/** Réponse de GET /api/relectures/en-attente — conforme au contrat api/contrat.yaml. */
public record RelectureEnAttenteDto(
        Long relectureId,
        Long exerciceId,
        String lien,
        String sessionTitre) {
}