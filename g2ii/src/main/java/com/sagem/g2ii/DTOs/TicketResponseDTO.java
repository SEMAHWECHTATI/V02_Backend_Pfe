package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponseDTO {

    private Long idTicket;
    private String reference;
    private String titre;
    private String description;
    private String type;
    private Priorite priorite;
    private StatutTicket statut;
    private LocalDate date;

    private LocalDate datePriseEncharge;
    private LocalDate dateResolution;
    private LocalDate dateCloture;

    private double delaiResolution;
    private Boolean slaRespecte;
    private String noteResolution;

    private String demandeurEmail;
    private String demandeurNom;
    private String technicienEmail;
    private String groupeNom;
    private String categorieName;

    private int nombreNotes;
    private int nombrePiecesJointes;
}