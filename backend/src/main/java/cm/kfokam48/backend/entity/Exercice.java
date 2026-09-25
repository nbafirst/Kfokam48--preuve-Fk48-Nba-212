package cm.kfokam48.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "exercice")
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    public SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    public Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relecteur_id")
    public Etudiant relecteur;

    @OneToOne(mappedBy = "exercice", fetch = FetchType.LAZY)
    public Relecture relecture;

    @Column(nullable = false, length = 2048)
    public String lien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    public StatutExercice statut;

    @Column(name = "depose_at", nullable = false)
    public Instant deposeAt;

    @Column(name = "lien_remplace_at")
    public Instant lienRemplaceAt;
}
