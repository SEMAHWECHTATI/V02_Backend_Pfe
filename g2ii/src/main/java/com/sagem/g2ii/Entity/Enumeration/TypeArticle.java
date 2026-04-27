package com.sagem.g2ii.Entity.Enumeration;


public enum TypeArticle {
    PIECE_RECHANGE("Pièce de rechange"),
    CONSOMMABLE("Consommable"),
    EQUIPEMENT("Équipement"),
    RESEAU("Équipement réseau"),
    SERVEUR("Serveur"),
    MATERIEL_ROULANT("Matériel roulant"),
    PERIPHERIQUE("Périphérique");

    private final String label;

    TypeArticle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
