package cm.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Requête de POST /api/presences — conforme au contrat api/contrat.yaml. */
public record PresenceCreationDto(

        @NotBlank(message = "Le code de présence est obligatoire.")
        String code,

        @NotNull(message = "L'étudiant est obligatoire.")
        Long etudiantId) {
}
