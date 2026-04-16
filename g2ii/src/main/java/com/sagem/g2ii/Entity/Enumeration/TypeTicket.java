package com.sagem.g2ii.Entity.Enumeration;

public enum TypeTicket {

    INTERVENTION_RESEAUX("Intervention Réseau", "Problèmes de connectivité, routeur, switch"),
    DEMANDE_MATERIEL("Demande de Matériel", "Ordinateur, écran, clavier, souris"),
    DEROGATION("Dérogation", "Demande d'exception ou d'autorisation spéciale"),
    DEMANDE_SERVICE("Demande de Service IT", "Création compte, droits d'accès, configuration"),
    INTERVENTION_INFORMATIQUE("Intervention Informatique", "Maintenance préventive, réparation"),
    AMELIORATION("Amélioration", "Suggestion d'optimisation ou nouvelle fonctionnalité"),
    INCIDENT_CRITIQUE("Incident Critique", "Problème affectant plusieurs utilisateurs");

    private final String label;
    private final String description;

    TypeTicket(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }

    /**
     * Récupérer le groupe responsable selon le type
     */
    public GroupeTechnicien getGroupeResponsable() {
        return switch (this) {
            case INTERVENTION_RESEAUX, INCIDENT_CRITIQUE -> GroupeTechnicien.IT_Reseaux_Informatique;
            case DEMANDE_MATERIEL -> GroupeTechnicien.IT_Gestionnaire_Stock;
            case INTERVENTION_INFORMATIQUE -> GroupeTechnicien.IT_Maintenance_Informatique;
            case DEMANDE_SERVICE -> GroupeTechnicien.IT_Tracabilite_Produit;
            case DEROGATION, AMELIORATION -> GroupeTechnicien.IT_Management;
        };
    }

    public int getDelaiSLAParDefaut() {
        return switch (this) {
            case INCIDENT_CRITIQUE -> 1;      // 1 heure
            case INTERVENTION_RESEAUX -> 2;   // 2 heures
            case DEMANDE_SERVICE -> 4;        // 4 heures
            case INTERVENTION_INFORMATIQUE -> 8;
            case DEMANDE_MATERIEL -> 120;     // 5 jours
            case DEROGATION -> 48;            // 2 jours
            case AMELIORATION -> 240;         // 10 jours
        };
    }

    public String getIcon() {
        return switch (this) {
            case INTERVENTION_RESEAUX -> "bi-wifi";
            case DEMANDE_MATERIEL -> "bi-pc-display";
            case DEROGATION -> "bi-file-earmark";
            case DEMANDE_SERVICE -> "bi-gear";
            case INTERVENTION_INFORMATIQUE -> "bi-tools";
            case AMELIORATION -> "bi-lightbulb";
            case INCIDENT_CRITIQUE -> "bi-exclamation-circle";
        };
    }

    public String getCouleur() {
        return switch (this) {
            case INTERVENTION_RESEAUX -> "#3b82f6";
            case DEMANDE_MATERIEL -> "#10b981";
            case DEROGATION -> "#f59e0b";
            case DEMANDE_SERVICE -> "#8b5cf6";
            case INTERVENTION_INFORMATIQUE -> "#ef4444";
            case AMELIORATION -> "#06b6d4";
            case INCIDENT_CRITIQUE -> "#dc2626";
        };
    }
}