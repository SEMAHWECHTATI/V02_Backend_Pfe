package com.sagem.g2ii.Entity.Enumeration;

public enum StatutTicket {
    Nouveau("Nouveau", "Le ticket vient d'être créé"),
    En_Cours("En cours", "Le ticket est en traitement"),
    En_Attente("En attente", "En attente de feedback ou ressources"),
    Resolu("Résolu", "Le problème est résolu"),
    Cloture("Clôturé", "Le ticket est définitivement fermé");

    private final String label;
    private final String description;

    StatutTicket(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }
}