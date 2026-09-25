package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.SessionCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionCoursRepository extends JpaRepository<SessionCours, Long> {

    Optional<SessionCours> findByCode(String code);
}
