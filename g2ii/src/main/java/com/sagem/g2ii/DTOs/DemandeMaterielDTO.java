package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Entity.Enumeration.TypeDemande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeMaterielDTO {
    private Long id;
    private String reference;
    private Long articleId;
    private String articleReference;
    private String articleDesignation;
    private Integer quantiteDemandee;
    private TypeDemande type;
    private StatutDemande statut;
    private String justification;
    private Long utilisateurDemandeurId;
    private String utilisateurDemandeurName;
    private Long utilisateurGestionnaireId;
    private String utilisateurGestionnaireName;
    private LocalDateTime dateValidationGestionnaire;
    private Long utilisateurAdminId;
    private String utilisateurAdminName;
    private LocalDateTime dateValidationAdmin;
    private String motifRejet;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateConsommation;
    private String referenceTicket;
}