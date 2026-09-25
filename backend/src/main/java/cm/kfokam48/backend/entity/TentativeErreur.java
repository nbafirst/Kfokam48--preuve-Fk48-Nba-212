package cm.kfokam48.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

/** RG3 — une tentative de code erroné (inconnu ou expiré), pour le blocage 2 min. */
@Entity
@Table(name = "tentative_erreur")
public class TentativeErreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    public Etudiant etudiant;

    @Column(name = "survenue_at", nullable = false)
    public Instant survenueAt;
}
