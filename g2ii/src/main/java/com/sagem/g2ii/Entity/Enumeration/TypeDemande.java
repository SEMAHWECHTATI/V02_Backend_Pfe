package com.sagem.g2ii.Entity.Enumeration;

public enum TypeDemande {
    CONSOMMABLE("Consommable"),
    EQUIPEMENT("Équipement"),
    REPARATION("Réparation");

    private final String label;

    TypeDemande(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}