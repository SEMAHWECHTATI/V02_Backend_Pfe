package com.sagem.g2ii.Entity.Enumeration;

public enum TypeMateriel {
    INFORMATIQUE("Informatique"),
    MOBILIER("Mobilier"),
    FOURNITURES("Fournitures"),
    EQUIPEMENT("Équipement"),
    AUTRE("Autre");

    private final String label;

    TypeMateriel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}