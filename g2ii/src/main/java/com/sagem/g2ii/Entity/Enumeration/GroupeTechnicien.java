package com.sagem.g2ii.Entity.Enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupeTechnicien {
    IT_Reseaux_Informatique("IT_Reseaux_Informatique", "IT Réseaux Informatique"),
    IT_Maintenance_Informatique("IT_Maintenance_Informatique", "IT Maintenance Informatique"),
    IT_Tracabilite_Produit("IT_Tracabilite_Produit", "IT Traçabilité Produit"),
    IT_Gestionnaire_Stock("IT_Gestionnaire_Stock", "IT Gestionnaire Stock"),
    IT_Management("IT_Management", "IT Management"),
    Demandeur("Demandeur", "Demandeur");

    private final String code;
    private final String label;

    GroupeTechnicien(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static GroupeTechnicien fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code du groupe ne peut pas être vide");
        }

        for (GroupeTechnicien groupe : GroupeTechnicien.values()) {
            if (groupe.code.equalsIgnoreCase(code.trim())) {
                return groupe;
            }
        }
        throw new IllegalArgumentException("Groupe invalide: " + code);
    }

    public static boolean exists(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        for (GroupeTechnicien groupe : GroupeTechnicien.values()) {
            if (groupe.code.equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }
}