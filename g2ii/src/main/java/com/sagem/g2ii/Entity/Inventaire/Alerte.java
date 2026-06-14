package com.sagem.g2ii.Entity.Inventaire;

import com.sagem.g2ii.Entity.Enumeration.Severite;
import com.sagem.g2ii.Entity.Enumeration.StatutAlerte;
import com.sagem.g2ii.Entity.Enumeration.TypeAlerte;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alertes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severite severite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAlerte statut = StatutAlerte.NOUVELLE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateAcquittement;

    @OneToMany(mappedBy = "alerte", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    // 💡 Ajoutez @Builder.Default pour forcer Lombok à initialiser la liste
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "alerte_article",
            joinColumns = @JoinColumn(name = "alerte_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    private List<Article> articles = new ArrayList<>(); // 👈 Ajoutez "= new ArrayList<>()" ici

    @PrePersist
    public void prePersist() {
        dateCreation = LocalDateTime.now();
    }

    // 💡 Méthode Helper pour sécuriser l'ajout d'un article
    public void addArticle(Article article) {
        if (this.articles == null) {
            this.articles = new ArrayList<>();
        }
        this.articles.add(article);
    }
}