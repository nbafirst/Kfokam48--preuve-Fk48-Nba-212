package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.TentativeErreur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface TentativeErreurRepository extends JpaRepository<TentativeErreur, Long> {

    long countByEtudiantIdAndSurvenueAtAfter(Long etudiantId, Instant depuis);

    void deleteByEtudiantId(Long etudiantId);
}
