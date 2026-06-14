package com.sagem.g2ii.Entity.Enumeration;


public enum TypeArticle {
    PIECE_RECHANGE("Pièce de rechange"),
    CONSOMMABLE("Consommable"),
    EQUIPEMENT("Équipement"),
    RESEAU("Équipement réseau"),
    SERVEUR("Serveur"),
    MATERIEL_ROULANT("Matériel roulant"),
    PERIPHERIQUE("Périphérique"),
    SERIALISE(""),  // Pour les PC, Serveurs, Smartphones
    QUANTITE("") ;   // Pour les consommables (Souris, Câbles, Toners)

    private final String label;

    TypeArticle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
