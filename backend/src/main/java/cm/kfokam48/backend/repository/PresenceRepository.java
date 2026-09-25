package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {
}
