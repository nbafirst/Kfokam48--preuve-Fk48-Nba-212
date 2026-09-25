package cm.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Requête de POST /api/sessions — conforme au contrat api/contrat.yaml. */
public record SessionCreationDto(

        @NotBlank(message = "Le titre est obligatoire.")
        String titre,

        @NotNull(message = "La promotion est obligatoire.")
        Long promotionId) {
}
