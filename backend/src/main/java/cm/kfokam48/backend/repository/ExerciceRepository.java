package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Exercice> findByEtudiantId(Long etudiantId);

    @Query("SELECT e FROM Exercice e WHERE e.session.promotion.id = ?1")
    List<Exercice> findByPromotionId(Long promotionId);
}
