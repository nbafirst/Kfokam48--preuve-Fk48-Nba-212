package cm.kfokam48.backend.repository;

import cm.kfokam48.backend.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
}
