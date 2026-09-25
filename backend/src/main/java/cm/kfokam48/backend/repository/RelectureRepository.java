package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    List<Relecture> findByExerciceIdOrderByNumero(Long exerciceId);

    Optional<Relecture> findByExerciceIdAndNumero(Long exerciceId, Integer numero);

    List<Relecture> findByRelecteurIdAndRendueAtIsNull(Long relecteurId);

    List<Relecture> findByRendueAtIsNull();

    long countByExerciceId(Long exerciceId);

    boolean existsByExerciceIdAndRelecteurId(Long exerciceId, Long relecteurId);
}