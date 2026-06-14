package com.sagem.g2ii.Entity.Inventaire;


import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_localisations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleLocalisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "localisation_id", nullable = false)
    private Localisation localisation;

    @Column(nullable = false)
    private Integer quantite = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutArticle statut = StatutArticle.ACTIF ;  // ACTIF, EN_REPARATION, A_RECYCLER

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateIntroduction;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateIntroduction = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (quantite == null) {
            quantite = 0;
        }
        if (statut == null) {
            statut = StatutArticle.ACTIF;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
