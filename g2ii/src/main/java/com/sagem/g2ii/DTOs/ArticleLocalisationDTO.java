package com.sagem.g2ii.DTOs;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLocalisationDTO {
    private String id;
    private String localisationId;
    private String localisationNom;
    private Integer quantite;
    private String statut;
    private LocalDateTime dateIntroduction;
}
