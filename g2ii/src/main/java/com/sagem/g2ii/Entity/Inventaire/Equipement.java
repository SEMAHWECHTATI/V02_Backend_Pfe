package com.sagem.g2ii.Entity.Inventaire;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "article_id")
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEquipement statut; // ACTIF, EN_REPARATION, A_RECYCLER

    @ManyToOne
    @JoinColumn(name = "localisation_id")
    private Localisation localisation;

    @Column(length = 500)
    private String observations;

    @Column(name = "date_acquisition")
    private LocalDateTime dateAcquisition;

    @Column(name = "date_mise_au_rebut")
    private LocalDateTime dateMiseAuRebut;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "updated_at")
    private LocalDateTime dateModification;

    @Column(name = "created_by", length = 100)
    private String creePar;

    @ManyToOne
    @JoinColumn(name = "utilisateur_responsable_id")
    private Utilisateur responsable;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (statut == null) {
            statut = StatutEquipement.ACTIF;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public enum StatutEquipement {
        ACTIF,
        EN_REPARATION,
        A_RECYCLER
    }
}