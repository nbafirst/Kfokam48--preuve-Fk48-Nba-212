package cm.kfokam48.backend.dto;

import jakarta.validation.constraints.NotNull;

/** Requête de PUT /api/exercices/{id}/lien — conforme au contrat api/contrat.yaml. */
public record LienRemplaceDto(

        @NotNull(message = "Le lien est obligatoire.")
        String lien) {
}