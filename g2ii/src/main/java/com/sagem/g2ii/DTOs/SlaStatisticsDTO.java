package com.sagem.g2ii.DTOs;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaStatisticsDTO {
    private long totalTicketsAvecSla;
    private long slaGlobauxRespectes;
    private long slaGlobauxDepasses;
    private double tauxReussiteGlobal; // Pourcentage (ex: 85.5%)

    // Détails par étape ITIL
    private long reussitePriseEnCharge;
    private long echecPriseEnCharge;
    private long reussiteResolution;
    private long echecResolution;

    // Répartition par Priorité (ex: CRITIQUE -> 90%, HAUTE -> 85%)
    private Map<String, Double> tauxReussiteParPriorite;
}