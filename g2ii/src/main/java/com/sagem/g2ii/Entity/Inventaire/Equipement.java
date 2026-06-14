package com.sagem.g2ii.Entity.Inventaire;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String numeroSerie;

    @Column(nullable = false, length = 255)
    private String designation;

    @ManyToOne
    @JoinColumn(name = "article_id")
    @JsonIgnoreProperties("equipements") // 👈 Sécurité supplémentaire
    private Article article;

    // Enregistrement du statut sous forme de chaîne de caractères (ACTIF, EN_REPARATION, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatutArticle statut;

    // Relation avec la localisation physique
    @ManyToOne
    @JoinColumn(name = "localisation_id")
    private Localisation localisation;

    @Column(length = 500)
    private String observations;

    @Column(name = "date_acquisition")
    private LocalDateTime dateAcquisition;

    @Column(nullable = false, unique = true, length = 150)
    private String codeBarres;

    @Column(name = "date_mise_au_rebut")
    private LocalDateTime dateMiseAuRebut;

    // Utilisation des annotations Hibernate pour harmoniser avec l'entité Localisation
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime dateModification;

    @Column(name = "created_by", length = 100)
    private String creePar;

    @ManyToOne
    @JoinColumn(name = "utilisateur_responsable_id")
    private Utilisateur responsable;

    @PrePersist
    protected void onCreate() {
        // Gestion de la valeur par défaut du statut si non spécifié
        if (this.statut == null) {
            this.statut = StatutArticle.ACTIF;
        }

        // Génération automatique du code-barres basé sur le numéro de série
        if (this.codeBarres == null && this.numeroSerie != null) {
            this.codeBarres =  this.numeroSerie.trim().toUpperCase();
        }
    }
}