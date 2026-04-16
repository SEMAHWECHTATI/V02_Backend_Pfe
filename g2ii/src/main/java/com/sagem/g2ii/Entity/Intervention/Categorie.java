package com.sagem.g2ii.Entity.Intervention;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Enumeration.TypeTicket;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Categorie_Ticket")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategorie;

    @Column(length = 100, nullable = false)
    private String nomCategorie;

    @Column(length = 500)
    private String descriptionCategorie;

    @Enumerated(EnumType.STRING)
    private TypeTicket type;

    @Column(nullable = false)
    private Boolean actif = true;

    // ✅ RELATION : Chaque catégorie est liée à UN groupe responsable
    @ManyToOne
    @JoinColumn(name = "id_groupe_responsable", nullable = false)
    private Groupe groupeResponsable;

    // ✅ Une catégorie a PLUSIEURS SLA
    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
    private List<SLA> slas;

    // ✅ Une catégorie a PLUSIEURS tickets
    @OneToMany(mappedBy = "categorie")
    @JsonIgnoreProperties("categorie")
    private List<Ticket> tickets;
}