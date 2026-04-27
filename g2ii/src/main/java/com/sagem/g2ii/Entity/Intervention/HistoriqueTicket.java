package com.sagem.g2ii.Entity.Intervention;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Historique_Ticket")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoriqueTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistoriqueTicket;

    @Column(length = 100, nullable = false)
    private String champModifie;

    @Column(length = 200)
    private String ancienneValeur;

    @Column(length = 200)
    private String nouvelleValeur;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;
}