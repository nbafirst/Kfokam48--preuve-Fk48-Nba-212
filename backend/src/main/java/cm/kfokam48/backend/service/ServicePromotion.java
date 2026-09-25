package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.EtudiantPromotionDto;
import cm.kfokam48.backend.entity.Etudiant;
import cm.kfokam48.backend.entity.Promotion;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.EtudiantRepository;
import cm.kfokam48.backend.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour la promotion : liste des étudiants (Q1, EF2, H3).
 */
@Service
public class ServicePromotion {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public ServicePromotion(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    @Transactional(readOnly = true)
    public List<EtudiantPromotionDto> listerEtudiants(Long promotionId) {
        promotions.findById(promotionId)
                .orElseThrow(() -> new ApiException("PROMOTION_INCONNUE",
                        "Cette promotion n'existe pas.", 404));

        return etudiants.findByPromotionId(promotionId).stream()
                .map(e -> new EtudiantPromotionDto(e.id, e.nom))
                .toList();
    }
}