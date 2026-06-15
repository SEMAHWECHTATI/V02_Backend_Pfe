package com.sagem.g2ii.DTOs;

import lombok.Data;
import java.util.Map;

@Data
public class KPIInterventionDTO {
    private long totalInterventions;
    private double tempsMoyenResolution; // En heures
    private double tauxCloture;         // En pourcentage
    private long interventionsEnRetardSLA;

    private Map<String, Long> interventionsParPeriode;
    private Map<String, Long> repartitionParDomaine;   // Clé : Nom Catégorie
    private Map<String, Long> repartitionParDemandeur; // Clé : Nom + Prénom

    private Map<String, Double> tempsMoyenParDomaine;    // Clé: Domaine, Valeur: Temps en hrs
    private Map<String, Double> tempsMoyenParTechnicien; // Clé: Technicien, Valeur: Temps en hrs
}