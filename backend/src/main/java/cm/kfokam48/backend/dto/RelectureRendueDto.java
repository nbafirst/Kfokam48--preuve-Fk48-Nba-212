package cm.kfokam48.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Requête de POST /api/relectures/{id} — conforme au contrat api/contrat.yaml. */
public record RelectureRendueDto(

        @NotNull(message = "La note est obligatoire.")
        @Min(value = 0, message = "La note doit être entre 0 et 20.")
        @Max(value = 20, message = "La note doit être entre 0 et 20.")
        Integer note,

        @NotNull(message = "Le commentaire est obligatoire.")
        String commentaire) {
}