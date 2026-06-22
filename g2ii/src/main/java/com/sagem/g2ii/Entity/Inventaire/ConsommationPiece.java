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

    // Modifiez FetchType.LAZY en FetchType.EAGER
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "article_id", nullable = false)
    @JsonIgnoreProperties({"consommations", "stocks", "tickets", "fournisseur", "localisation", "alertes", "equipements"})
    private Article article;

    // Faites de même pour le responsable
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsable_id", nullable = false)
    @JsonIgnoreProperties({"password", "roles", "tickets", "consommations"})
    private Utilisateur responsable;

    @PrePersist
    public void prePersist() {
        this.dateConsommation = LocalDateTime.now();
    }
}