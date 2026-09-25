package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurIdAndRendueAtIsNull(Long relecteurId);

    List<Relecture> findByRendueAtIsNull();
}