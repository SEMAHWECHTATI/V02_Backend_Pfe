package com.sagem.g2ii.Entity.Enumeration;


public enum StatutAlerte {
    NOUVELLE("Nouvelle"),
    LUE("Lue"),
    TRAITEE("Traitée");

    private final String label;

    StatutAlerte(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}