package com.sagem.g2ii.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDTO {

    private Long id;

    private String codeBarresArticle;

    private Integer quantiteEnStock;

    private Integer quantiteCritique;

    private Integer quantiteMinimum;

    private BigDecimal prixUnitaire;

    // Relations
    private Long articleId;

    private String articleReference;

    private String articleDesignation;

    private String articleTypeArticle;

    private String articleStatut;

    // Informations calculées
    private BigDecimal valeurTotale;

    private Boolean estFaible;

    private Boolean estCritique;

    // Constructeurs personnalisés pour faciliter la conversion
    @jakarta.persistence.Transient
    public BigDecimal getValeurTotal() {
        if (prixUnitaire == null || quantiteEnStock == null)
            return BigDecimal.ZERO;
        return prixUnitaire.multiply(new BigDecimal(quantiteEnStock));
    }
    @Transient
    public Boolean isEstFaible() {
        return quantiteEnStock != null && quantiteMinimum != null &&
                quantiteEnStock <= quantiteMinimum;
    }

    @Transient
    public Boolean isEstCritique() {
        return quantiteEnStock != null && quantiteCritique != null &&
                quantiteEnStock <= quantiteCritique;
    }
}