package com.sagem.g2ii.Entity.Enumeration;


public enum StatutNotification {
    EN_ATTENTE("En attente"),
    ENVOYEE("Envoyée"),
    ECHOUEE("Échouée");

    private final String label;

    StatutNotification(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}