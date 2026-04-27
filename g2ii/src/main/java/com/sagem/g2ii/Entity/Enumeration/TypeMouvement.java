package com.sagem.g2ii.Entity.Enumeration;

public enum TypeMouvement {
    ENTREE("Entrée en stock"),
    SORTIE("Sortie de stock"),
    TRANSFERT("Transfert"),
    PRET("Prêt"),
    RETOUR("Retour de prêt"),
    CONSOMMATION("Consommation"),
    AJUSTEMENT("Ajustement");

    private final String label;

    TypeMouvement(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}