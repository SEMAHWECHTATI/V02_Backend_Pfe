package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.TypeMouvement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStockDTO {
    private Long id;
    private TypeMouvement type;
    private Integer quantite;
    private String justification;
    private String localisationSource;
    private String localisationDestination;
    private LocalDateTime dateMouvement;
    private Long stockId;
    private String stockDesignation;
    private Long articleId;
    private String articleDesignation;
    private Long responsableId;
    private String responsableName;
    private String referenceTicket;
}
