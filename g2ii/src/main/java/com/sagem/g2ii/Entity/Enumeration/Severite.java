package com.sagem.g2ii.Entity.Enumeration;

public enum Severite {
    BASSE(1, "Basse"),
    MOYENNE(2, "Moyenne"),
    HAUTE(3, "Haute"),
    CRITIQUE(4, "Critique");

    private final int niveau;
    private final String label;

    Severite(int niveau, String label) {
        this.niveau = niveau;
        this.label = label;
    }

    public int getNiveau() {
        return niveau;
    }

    public String getLabel() {
        return label;
    }
}