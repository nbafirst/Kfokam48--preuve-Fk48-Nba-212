package cm.kfokam48.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "presence")
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    public SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    public Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    public SourcePresence source;

    @Column(name = "cree_at", nullable = false)
    public Instant creeAt;
}
