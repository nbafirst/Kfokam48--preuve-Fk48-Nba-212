package cm.kfokam48.backend.dto;

import java.time.Instant;

/** Réponse de POST /api/sessions/{id}/cloture — conforme au contrat api/contrat.yaml. */
public record SessionClotureeDto(
        Long id,
        Instant clotureAt) {
}