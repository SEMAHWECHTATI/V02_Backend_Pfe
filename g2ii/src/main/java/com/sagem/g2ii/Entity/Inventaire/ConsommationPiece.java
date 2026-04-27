package com.sagem.g2ii.Entity.Inventaire;

import jakarta.persistence.*;
import lombok.*;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import java.time.LocalDateTime;

@Entity
@Table(name = "consommations_pieces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsommationPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantite;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateConsommation;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur responsable;

    @Column(length = 100)
    private String referenceTicket;

    @PrePersist
    public void prePersist() {
        dateConsommation = LocalDateTime.now();
    }
}