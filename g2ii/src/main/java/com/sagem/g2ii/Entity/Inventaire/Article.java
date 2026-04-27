package com.sagem.g2ii.Entity.Inventaire;

import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 200)
    private String designation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 100)
    private String codeBarres;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeArticle typeArticle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutArticle statut;

    @Column(nullable = false)
    private Integer quantiteEnStock = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    @Column(length = 100)
    private String fournisseur;

    private LocalDate dateAchat;

    private LocalDate dateGarantie;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @Column(nullable = false)
    private Integer seuilMinimum = 5;

    @Column(nullable = false)
    private Integer seuilCritique = 2;

    // Valeur totale de l'inventaire
    @Transient
    public BigDecimal getValeurTotal() {
        if (prixUnitaire == null || quantiteEnStock == null)
            return BigDecimal.ZERO;
        return prixUnitaire.multiply(new BigDecimal(quantiteEnStock));
    }

    @ManyToOne
    @JoinColumn(name = "localisation_id")
    private Localisation localisation;

    @ManyToMany(mappedBy = "articles")
    private List<Alerte> alertes;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<Stock> stocks;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private List<ConsommationPiece> consommations;

    @PrePersist
    public void prePersist() {
        dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
    }
}
