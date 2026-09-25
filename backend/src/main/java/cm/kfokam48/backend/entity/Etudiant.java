package cm.kfokam48.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "promotion_id", nullable = false, insertable = false, updatable = false)
    public Long promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    public Promotion promotion;

    @Column(nullable = false)
    public String nom;
}
