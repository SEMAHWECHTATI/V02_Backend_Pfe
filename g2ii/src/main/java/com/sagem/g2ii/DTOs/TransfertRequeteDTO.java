package com.sagem.g2ii.DTOs;

import lombok.Data;

@Data
public class TransfertRequeteDTO {
    private Long stockId;
    private Integer quantite;
    private String locSource;
    private String locDest;
    private String justification;
    private Long responsableId;   // 👈 Obligatoire
    private String referenceTicket; // 👈 Recommandé
}