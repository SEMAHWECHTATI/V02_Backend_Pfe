package com.sagem.g2ii.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class KPIInventoryDTO {
    private double rotationStock;
    private double tauxDisponibilite;
    private double valeurTotalePatrimoineIT;
    private long articlesEnRupture;
    private double tauxUtilisationMateriel;
}