package com.sagem.g2ii.Entity.Enumeration;

public enum TypeNote {
    COMMENTAIRE("Commentaire", "Commentaire général"),
    TECHNIQUE("Technique", "Détails techniques"),
    RESOLUTION("Résolution", "Note de résolution");

    private final String label;
    private final String description;

    TypeNote(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }
}