package com.sagem.g2ii.Entity.Intervention;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sagem.g2ii.Entity.Enumeration.Priorite;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SLA")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SLA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSLA;

    @Column(length = 100)
    private String nomSLA;

    @Column
    private int delaiResolutionHeure;

    @Column
    private int delaiPriseEnchargeHeur;

    @Enumerated(EnumType.STRING)
    private Priorite priorite;

    // ✅ Many SLA -> 1 Catégorie
    @ManyToOne
    @JoinColumn(name = "id_categorie", nullable = false)
    @JsonIgnore // 👈 AJOUTEZ CECI POUR COUPER LA BOUCLE
    private Categorie categorie;
}