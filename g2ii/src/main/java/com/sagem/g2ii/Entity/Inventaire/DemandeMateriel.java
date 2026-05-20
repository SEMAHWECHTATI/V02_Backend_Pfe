package com.sagem.g2ii.Entity.Inventaire;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Entity.Enumeration.TypeDemande;
import com.sagem.g2ii.Entity.Enumeration.TypeMateriel;
import com.sagem.g2ii.Entity.Inventaire.Article;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes_materiel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private Integer quantiteDemandee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDemande type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String justification;

    // Utilisateur qui demande le matériel
    @ManyToOne
    @JoinColumn(name = "utilisateur_demandeur_id", nullable = false)
    private Utilisateur utilisateurDemandeur;

    // Gestionnaire qui valide
    @ManyToOne
    @JoinColumn(name = "utilisateur_gestionnaire_id")
    private Utilisateur utilisateurGestionnaire;

    @Column
    private LocalDateTime dateValidationGestionnaire;

    // Administrateur qui valide
    @ManyToOne
    @JoinColumn(name = "utilisateur_admin_id")
    private Utilisateur utilisateurAdmin;

    @Column
    private LocalDateTime dateValidationAdmin;

    // Motif de rejet si applicable
    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    // Dates
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column
    private LocalDateTime dateModification;

    @Column
    private LocalDateTime dateConsommation;

    // Lien avec ticket si applicable
    @Column(length = 100)
    private String referenceTicket;

    @PrePersist
    public void prePersist() {
        dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
    }
}