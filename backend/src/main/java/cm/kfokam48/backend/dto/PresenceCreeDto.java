package cm.kfokam48.backend.dto;

/** Réponse de POST /api/presences — conforme au contrat api/contrat.yaml. */
public record PresenceCreeDto(
        Long id,
        Long sessionId,
        Long etudiantId,
        String source) {
}
