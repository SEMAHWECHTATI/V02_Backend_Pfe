package com.sagem.g2ii.Entity.Enumeration;

public enum StatutDemande {
    EN_ATTENTE("En attente"),
    APPROUVEE("Approuvée"),
    REJETEE("Rejetée"),
    EN_COURS_LIVRAISON("En cours de livraison"),
    LIVREE("Livrée"),
    ANNULEE("Annulée"),
    CONSOMME("consommer"),
    VALIDE_GESTIONNAIRE("valide gestionnaire"),
    REJETE("rejeter");

    private final String label;

    StatutDemande(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}