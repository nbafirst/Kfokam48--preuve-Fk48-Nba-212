package cm.kfokam48.backend.dto;

import java.time.Instant;

/** Réponse de POST /api/sessions — conforme au contrat api/contrat.yaml. */
public record SessionCreeDto(
        Long id,
        String code,
        Instant ouvertureAt,
        Instant expirationAt) {
}
