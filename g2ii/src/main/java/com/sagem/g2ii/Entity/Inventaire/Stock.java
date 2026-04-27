package com.sagem.g2ii.Entity.Inventaire;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String codeBarresArticle;

    @Column(nullable = false)
    private Integer quantiteEnStock = 0;

    @Column(nullable = false)
    private Integer quantiteCritique;

    @Column(nullable = false)
    private Integer quantiteMinimum;

    // ✅ SOLUTION 1 : Utiliser BigDecimal (Recommandé pour les prix)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;


    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private List<MouvementStock> mouvements;
}