package com.sagem.g2ii.Entity.Authentification;

import com.sagem.g2ii.Entity.Enumeration.departementService;
import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import com.sagem.g2ii.Entity.Enumeration.statutDemandeInscription;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor @Builder
@Table(name = "DemandeInscription")
public class DemandeInscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 50)
    private String matricule;

    @Column(length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private departementService departement;

    // Pour la validation par l'administrateur
    @Column(columnDefinition = "TEXT")
    private String motifDemande;

    @Enumerated(EnumType.STRING)
    @Column
    private roleUtilisateur roleDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private statutDemandeInscription statut = statutDemandeInscription.En_Attente;

    @Column(columnDefinition = "TEXT")
    private String motifRefus;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateDemande = LocalDateTime.now();

    @Column
    private LocalDateTime dateTraitement;

    @Column(columnDefinition = "boolean default false")
    private boolean archivee = false;

    // Logique pour gérer la date de traitement automatiquement
    @PreUpdate
    protected void onUpdate() {
        // Si le statut change (n'est plus 'En_Attente'), on met à jour la date de traitement
        if (this.statut != statutDemandeInscription.En_Attente && this.dateTraitement == null) {
            this.dateTraitement = LocalDateTime.now();
        }
    }

    @ManyToOne
    @JoinColumn(name = "groupe_id")
    private Groupe groupeARejoindre;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Utilisateur administrateur;
}