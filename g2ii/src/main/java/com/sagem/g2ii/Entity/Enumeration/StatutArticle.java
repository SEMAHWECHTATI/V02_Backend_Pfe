package com.sagem.g2ii.Entity.Enumeration;


public enum StatutArticle {
    ACTIF("Actif"),
    EN_REPARATION("En réparation"),
    EN_PANNE("En panne"),
    ARCHIVÉ("Archivé"),
    A_RECYCLER("À recycler"),
    OBSOLETE("Obsolète"),
    RUPTURE("Rupture de stock");


    private final String label;

    StatutArticle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}