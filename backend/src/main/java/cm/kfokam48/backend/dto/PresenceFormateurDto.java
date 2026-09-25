package cm.kfokam48.backend.dto;

import jakarta.validation.constraints.NotNull;

/** Requête de POST /api/sessions/{id}/presences — le formateur ajoute une présence (EF4). */
public record PresenceFormateurDto(

        @NotNull(message = "L'étudiant est obligatoire.")
        Long etudiantId) {
}
