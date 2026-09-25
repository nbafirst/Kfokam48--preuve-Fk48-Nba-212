package cm.kfokam48.backend.dto;

/** Réponse de GET /api/promotions/{id}/etudiants — conforme au contrat api/contrat.yaml. */
public record EtudiantPromotionDto(
        Long etudiantId,
        String nom) {
}