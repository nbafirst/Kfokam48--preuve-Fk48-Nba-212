package cm.kfokam48.backend.dto;

import java.util.List;

/** Réponse de GET /api/etudiants/{id}/resultats — conforme au contrat api/contrat.yaml (v1.1+). */
public record ResultatExerciceDto(
        Long exerciceId,
        String sessionTitre,
        String statut,              // EN_ATTENTE | ASSIGNE | RELU
        String statutNote,          // AUCUNE | PROVISIONAL | FINAL
        Integer nbEvaluations,      // 0, 1 ou 2
        Double moyenne,             // moyenne des notes (PROVISIONAL = note unique, FINAL = moyenne des 2)
        Integer noteUnique,         // note si PROVISIONAL (une seule évaluation)
        String commentaireUnique,   // commentaire si PROVISIONAL
        List<EvaluationDto> evaluations // liste des 2 évaluations si FINAL
) {
    public record EvaluationDto(Integer note, String commentaire) {}
}