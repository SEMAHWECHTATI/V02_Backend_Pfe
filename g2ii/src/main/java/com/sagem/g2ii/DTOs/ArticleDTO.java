package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
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
public class ArticleDTO {
    private Long id;
    private String categorie;
    private String reference;
    private String designation;
    private String description;
    private String codeBarres;
    private TypeArticle typeArticle;
    private StatutArticle statut;
    private Integer quantiteEnStock;
    private BigDecimal prixUnitaire;

    // Fournisseur relationnel aplati
    private Long fournisseurId;
    private String fournisseurNom;

    private LocalDate dateAchat;
    private LocalDate dateGarantie;
    private Integer seuilMinimum;
    private Integer seuilCritique;
    private Long localisationId;
    private String localisationLabel;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal valeurTotal;
}