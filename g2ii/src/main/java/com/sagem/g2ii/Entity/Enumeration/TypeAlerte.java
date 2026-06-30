package com.sagem.g2ii.Entity.Enumeration;

public enum TypeAlerte {

    INSCRIPTION("INSCRIPTION"),
    Stock_faible("stocke faible"),

    validation_compte("validation_compte"),
    depassement_sla("depassements_sla"),
    nouvelle_assignation("nouvelle assignation"),
    refus_compte("refuse de compte"),
    INFO("information"),
    Erreur("erreur"),
    WARNING("warning"),
    CRITICAL("critical"),
    RUPTURE_STOCK("Rupture de stock"),
    SEUIL_CRITIQUE("Seuil critique"),
    SEUIL_MINIMUM("Seuil minimum"),
    EXPIRATION("Date d'expiration"),
    AUTRE("Autre");

    private final String label;

    TypeAlerte(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
