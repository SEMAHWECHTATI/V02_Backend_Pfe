package com.sagem.g2ii.DTOs;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteTicketDTO {

    private String contenu;
    private String type; // COMMENTAIRE, TECHNIQUE, RESOLUTION

    private Long idTicket;
    private Long idUtilisateur;
}