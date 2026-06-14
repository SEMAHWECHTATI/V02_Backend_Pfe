package com.sagem.g2ii.DTOs;

import lombok.Data;

@Data
public class MouvementRequeteDTO {
    private Long stockId;
    private Integer quantite;
    private String justification;
    private Long responsableId;   // 👈 Obligatoire pour la traçabilité
    private String referenceTicket; // 👈 Recommandé / Optionnel
}