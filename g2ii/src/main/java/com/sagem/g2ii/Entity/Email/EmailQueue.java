package com.sagem.g2ii.Entity.Email;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter

public class EmailQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destinataire;
    private String sujet;
    @Column(columnDefinition = "TEXT")
    private String contenu;

    private boolean envoye = false; // Sera mis à true quand ça marche
    private int tentatives = 0;    // Nombre d'essais effectués
    private String dernierErreur;
}
