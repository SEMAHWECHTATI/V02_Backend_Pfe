package com.sagem.g2ii.Entity.Enumeration;

public enum Priorite {
    Basse("Basse", "Impact faible"),
    Moyenne("Moyenne", "Impact moyen"),
    Haute("Haute", "Impact important"),
    Critique("Critique", "Impact critique");

    private final String label;
    private final String description;

    Priorite(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }
}