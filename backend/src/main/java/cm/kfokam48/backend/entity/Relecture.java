package cm.kfokam48.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercice_id", nullable = false)
    public Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relecteur_id", nullable = false)
    public Etudiant relecteur;

    @Column(name = "note")
    public Integer note;

    @Column(name = "commentaire", length = 2048)
    public String commentaire;

    @Column(name = "rendue_at")
    public Instant rendueAt;
}