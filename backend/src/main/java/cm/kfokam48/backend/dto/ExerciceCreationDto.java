package cm.kfokam48.backend.dto;

import jakarta.validation.constraints.NotNull;

/** Requête de POST /api/exercices — conforme au contrat api/contrat.yaml. */
public record ExerciceCreationDto(

        @NotNull(message = "La session est obligatoire.")
        Long sessionId,

        @NotNull(message = "L'étudiant est obligatoire.")
        Long etudiantId,

        @NotNull(message = "Le lien est obligatoire.")
        String lien) {
}
