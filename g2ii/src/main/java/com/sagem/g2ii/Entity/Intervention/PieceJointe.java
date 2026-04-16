package com.sagem.g2ii.Entity.Intervention;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private LocalDate date = LocalDate.now();

    @Column(length = 100)
    private String typefichier;

    @ManyToOne
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;
}