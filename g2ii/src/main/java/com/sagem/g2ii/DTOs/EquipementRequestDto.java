package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EquipementRequestDto {
    private String numeroSerie;
    private String designation;
    private Long articleId;
    private Long localisationId;
    private StatutArticle statut; // Optionnel, sera ACTIF par défaut si null
    private String observations;
    private LocalDateTime dateAcquisition;
    private String creePar;
    private Long responsableId;
}