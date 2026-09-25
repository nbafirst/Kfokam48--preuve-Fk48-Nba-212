package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Long> findEtudiantIdsBySessionId(Long sessionId);

    @Query("SELECT p FROM Presence p WHERE p.session.promotion.id = ?1")
    List<Presence> findByPromotionId(Long promotionId);
}
