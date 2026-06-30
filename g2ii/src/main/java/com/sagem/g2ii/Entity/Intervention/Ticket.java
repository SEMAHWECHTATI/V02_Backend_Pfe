package com.sagem.g2ii.Entity.Intervention;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Ticket")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTicket;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private Priorite priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTicket statut = StatutTicket.Nouveau;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column
    private LocalDateTime datePriseEncharge;

    @Column
    private LocalDateTime dateResolution;

    @Column
    private LocalDateTime dateCloture;

    // Indicateurs de résultats SLA
    @Column
    private Boolean slaPriseEnChargeRespecte;

    @Column
    private Boolean slaResolutionRespecte;


    @Column
    private double delaiResolution;

    @Column
    private Boolean slaRespecte = true;

    @Column(length = 1000)
    private String noteResolution;

    @Column(name = "sla_prise_en_charge_depasse")
    private Boolean slaPriseEnChargeDepasse = false;

    @Column(name = "sla_resolution_depasse")
    private Boolean slaResolutionDepasse = false;

    @Column(name = "alerte_prise_en_charge_envoyee")
    private Boolean alertePriseEnChargeEnvoyee = false;

    // ===== RELATIONS =====

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categorie", nullable = false)
    @JsonIgnoreProperties({"tickets", "slas"}) // 👈 AJOUTEZ "slas" ici pour couper le triangle !
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_demandeur", nullable = false)
    @JsonIgnoreProperties({"groupes", "password"})
    private Utilisateur demandeur;

    @ManyToOne
    @JoinColumn(name = "id_technicien")
    private Utilisateur technicienAssigne;

    @ManyToOne
    @JoinColumn(name = "id_groupe")
    private Groupe groupeAssigne;

    // On autorise l'ajout (PERSIST) et la modification (MERGE), mais pas la suppression (REMOVE)
    @OneToMany(mappedBy = "ticket", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("ticket")
    private List<NoteTicket> notes;

    @OneToMany(mappedBy = "ticket", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("ticket")
    private List<PieceJointe> pieceJointes;

    @OneToMany(mappedBy = "ticket", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("ticket")
    private List<HistoriqueTicket> historiqueTickets;

    // ===== RELATIONS =====

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_sla")
    @JsonIgnoreProperties({"categorie", "tickets"})
    // 💡 Sans @ToString au niveau de la classe, l'annotation @ToString.Exclude est inutile et a été retirée d'ici.
    private SLA slaAssigne;
}