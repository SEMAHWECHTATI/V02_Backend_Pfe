package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Entity.Enumeration.TypeDemande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeMaterielDTO {

    // --- Informations de la Demande ---
    private Long id;
    private String reference;
    private Integer quantiteDemandee;
    private TypeDemande type;
    private StatutDemande statut;
    private String justification;
    private String referenceTicket;
    private String motifRejet;

    // --- Chronologie (Dates) ---
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateConsommation;
    private LocalDateTime dateValidationGestionnaire;
    private LocalDateTime dateValidationAdmin;

    // --- Informations Article (Aplaties pour faciliter la lecture côté Angular) ---
    private Long articleId;
    private String articleReference;
    private String articleDesignation;
    private String articleTypeArticle; // Ajouté pour l'UI
    private String articleCategorie;   // Ajouté pour l'UI
    private Integer articleQuantiteEnStock; // Ajouté pour l'UI
    private Integer articleSeuilMinimum;   // Ajouté pour l'UI (Alerte stock bas)
    private BigDecimal articlePrixUnitaire; // Ajouté pour l'UI

    // --- Informations Intervenants ---
    // Pour le demandeur : un sous-DTO avec toutes ses coordonnées
    private DemandeReponseDTO utilisateurDemandeur;

    // Pour les validateurs : les noms suffisent généralement pour la chronologie UI
    private Long utilisateurGestionnaireId;
    private String utilisateurGestionnaireNom;
    private String utilisateurGestionnairePrenom;

    private Long utilisateurAdminId;
    private String utilisateurAdminNom;
    private String utilisateurAdminPrenom;
}