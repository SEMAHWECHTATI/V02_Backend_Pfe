package com.sagem.g2ii.Entity.Enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupeTechnicien {
    IT_Reseaux_Informatique("IT_Reseaux_Informatique", "IT Réseaux Informatique"),
    IT_Maintenance_Informatique("IT_Maintenance_Informatique", "IT Maintenance Informatique"),
    IT_Tracabilite_Produit("IT_Tracabilite_Produit", "IT Traçabilité Produit"),
    IT_Gestionnaire_Stock("IT_Gestionnaire_Stock", "IT Gestionnaire Stock"),
    IT_Management("IT_Management", "IT Management"),
    Demandeur("Demandeur", "Demandeur"),
    IT_Devlopper("IT_Devlopper", "IT Devlopper"),
    IT_Devops("IT_Devops", "IT Devops"),
    Autre("Autre", "Autre"),
    IT_Cybersecurite("IT_Cybersecurite", "IT Cybersécurité"),
    IT_Helpdesk("IT_Helpdesk", "IT Support & Helpdesk"),
    IT_DBA("IT_DBA", "IT Administrateur de Bases de Données"),
    IT_Cloud("IT_Cloud", "IT Infrastructure & Cloud"),
    IT_Business_Analyst("IT_Business_Analyst", "IT Business Analyst"),
    IT_QA_Testing("IT_QA_Testing", "IT Assurance Qualité & Tests"),
    IT_ERP_Systems("IT_ERP_Systems", "IT Intégration ERP & SAP"),
    IT_Telecom("IT_Telecom", "IT Téléphonie & Télécom"),
    IT_Data_BI("IT_Data_BI", "IT Data & Business Intelligence"),
    IT_Formation("IT_Formation", "IT Formation & Support Utilisateurs");

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