package com.sagem.g2ii.Entity.Enumeration;


public enum CanalNotification {
    EMAIL("Email"),
    SMS("SMS"),
    PUSH("Notification push"),
    TABLEAU_BORD("Tableau de bord");

    private final String label;

    CanalNotification(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}