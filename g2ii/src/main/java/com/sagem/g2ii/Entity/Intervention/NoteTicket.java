package com.sagem.g2ii.Entity.Intervention;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.TypeNote;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "Note_Ticket")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNoteTicket;

    @Column(length = 1000, nullable = false)
    private String contenu;

    @Enumerated(EnumType.STRING)
    private TypeNote type = TypeNote.COMMENTAIRE;

    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    // ... reste du code intact ...
    @ManyToOne
    @JoinColumn(name = "id_ticket", nullable = false)
    @JsonIgnoreProperties({"notes", "historiqueTickets", "pieceJointes"}) // 👈 AJOUTER ICI
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;
}