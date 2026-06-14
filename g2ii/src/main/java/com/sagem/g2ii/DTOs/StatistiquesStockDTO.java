package com.sagem.g2ii.DTOs;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesStockDTO {
    private String dateGeneree;

    // Quantités
    private Integer totalArticles;
    private Integer totalQuantite;
    private Integer articlesEnRupture;
    private Integer articlesStockFaible;
    private Integer articlesStockCritique;

    // Finances
    private Double valeurTotaleBrutale;
    private Double valeurTotaleNette;
    private Double valeurEnReparation;
    private Double valeurARecycler;

    // Mouvements
    private Integer nombreMouvementsJour;
    private Integer nombreMouvementsMois;
    private Integer nombreAlertes;
    private Integer alertesCritiques;

    // Top articles
    private Integer nombreFournisseurs;
    private Integer nombreLocalisations;
}
