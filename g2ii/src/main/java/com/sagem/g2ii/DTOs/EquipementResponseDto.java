package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import lombok.Data;

@Data
public class EquipementResponseDto {
    private Long id;
    private String codeBarres;
    private String designation;
    private StatutArticle statut;

    // Données aplaties de l'Article associé
    private Long articleId;
    private String articleReference;
    private String articleDesignation;

    // Données aplaties de la Localisation associée
    private Long localisationId;
    private String localisationNom;
    private String localisationBatiment; // 👈 Ajouter
    private String localisationEtage;    // 👈 Ajouter
    private String localisationBureau;
}