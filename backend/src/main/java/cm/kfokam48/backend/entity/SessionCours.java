package cm.kfokam48.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "session_cours")
public class SessionCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    public Promotion promotion;

    @Column(nullable = false)
    public String titre;

    @Column(nullable = false, unique = true)
    public String code;

    @Column(name = "ouverture_at", nullable = false)
    public Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    public Instant expirationAt;

    @Column(name = "cloture_at")
    public Instant clotureAt;

    public boolean estCloturee() {
        return clotureAt != null;
    }
}
