package com.sagem.g2ii.Entity.Authentification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit; // Tu dois créer cet Enum
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Enumeration.TypeAlerte;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisateur qui a effectué l'action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 🌟 AJOUTE CETTE LIGNE !
    private Utilisateur utilisateur;

    // Type d'action
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionAudit action;


    // Module concerné
    @Enumerated(EnumType.STRING) // 🌟 AJOUTE CETTE LIGNE ICI !
    @Column(nullable = false, length = 50)
    private ModuleAudit module;

    // Entité concernée
    @Column(nullable = false, length = 50)
    private String entite;

    // Identifiant de l'entité
    private Long entiteId;

    // Description
    @Column(columnDefinition = "TEXT")
    private String description;

    // Anciennes valeurs
    @Column(columnDefinition = "TEXT")
    private String ancienneValeur;

    // Nouvelles valeurs
    @Column(columnDefinition = "TEXT")
    private String nouvelleValeur;

    // Adresse IP
    private String adresseIp;

    // Navigateur
    private String userAgent;

    // Niveau
    // Niveau
    @Enumerated(EnumType.STRING)
    private NiveauAudit niveau; // 🟢 Utilise NiveauAudit ici !

    // Succès ou échec
    private Boolean succes;

    // Date
    private LocalDateTime dateAction;
}