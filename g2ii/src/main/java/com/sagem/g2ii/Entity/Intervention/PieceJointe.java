package com.sagem.g2ii.Entity.Intervention;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Piece_Jointe")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idJointe;

    @Column(length = 200, nullable = false)
    private String nomJointe;

    @Column(length = 500)
    private String cheminStockage;

    @Column
    private long taille;

    @Column
    private LocalDateTime date = LocalDateTime.now();

    @Column(length = 100)
    private String typefichier;

    // ===== RELATIONS =====

    // ... reste du code intact ...
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_ticket", nullable = false)
    @JsonIgnoreProperties({"pieceJointes", "notes", "historiqueTickets"}) // 👈 AJOUTER ICI
    private Ticket ticket;

    // ✅ AJOUTER ID UTILISATEUR
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = true)
    private Utilisateur utilisateur;

    // ✅ Getters et Setters custom si nécessaire
    public Long getIdUtilisateur() {
        return utilisateur != null ? utilisateur.getId() : null;
    }

    public String getNomUtilisateur() {
        return utilisateur != null ? utilisateur.getPrenom() + " " + utilisateur.getNom() : "Inconnu";
    }
}