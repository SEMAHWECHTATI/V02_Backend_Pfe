package com.sagem.g2ii.DTOs;

import lombok.Data;
import java.util.Map;

@Data
public class KPIPerformanceDTO {
    private Map<String, Long> interventionsParTechnicien; // Clé : Email ou Nom
    private String domaineLePlusDemande;
    private double coutMoyenIntervention;
    private double scoreSatisfactionMoyen;
}