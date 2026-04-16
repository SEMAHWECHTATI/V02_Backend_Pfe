package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.Priorite;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TicketCreationDTO {

    private String titre;
    private String description;
    private Priorite priorite;

    private Long demandeurId;
    private Long categorieId;
    private Long groupeId;
}