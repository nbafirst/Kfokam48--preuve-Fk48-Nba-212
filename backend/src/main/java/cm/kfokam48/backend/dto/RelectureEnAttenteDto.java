package cm.kfokam48.backend.dto;

import java.lang.Short;

/** Réponse de GET /api/relectures/en-attente — conforme au contrat api/contrat.yaml (v1.1+). */
public record RelectureEnAttenteDto(
        Long relectureId,
        Long exerciceId,
        Short numero,          // 1 ou 2 (nouveau : deux relecteurs par exercice)
        String lien,
        String sessionTitre) {
}