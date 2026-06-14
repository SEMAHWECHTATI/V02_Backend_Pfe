package com.sagem.g2ii.Entity.Inventaire;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

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

    // Correspond au champ 'referenceTicket' envoyé par Angular
    @Column(name = "reference_ticket", nullable = false, length = 50)
    private String referenceTicket;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateConsommation;

    // Jointure ManyToOne vers l'Article
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    // ⬇️ AJOUTE "hibernateLazyInitializer" et "handler" dans la liste :
    @JsonIgnoreProperties({"consommations", "stocks", "tickets", "hibernateLazyInitializer", "handler"})
    private Article article;

    // Jointure ManyToOne vers l'Utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    // ⬇️ AJOUTE "hibernateLazyInitializer" et "handler" ici aussi :
    @JsonIgnoreProperties({"password", "roles", "tickets", "consommations", "hibernateLazyInitializer", "handler"})
    private Utilisateur responsable;

    @PrePersist
    public void prePersist() {
        this.dateConsommation = LocalDateTime.now();
    }
}