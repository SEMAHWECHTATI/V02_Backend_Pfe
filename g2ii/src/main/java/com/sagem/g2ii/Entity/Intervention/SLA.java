package com.sagem.g2ii.Entity.Intervention;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Enumeration.Priorite;
import jakarta.persistence.*;
import lombok.*;

/**
 * ✅ Entité SLA (Service Level Agreement)
 * Définit les délais de prise en charge et de résolution par Catégorie et Priorité
 */
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

    /**
     * Délai maximum de résolution en heures
     * Exemple: 24h pour un ticket de haute priorité
     */
    @Column(nullable = false)
    private int delaiResolutionHeure;

    /**
     * ✅ CORRIGÉ: Nom de colonne cohérent (minuscule "d")
     * Délai maximum de prise en charge en heures
     * Exemple: 4h pour un ticket de haute priorité
     */
    @Column(nullable = false)
    private int delaiPriseEnChargeHeure;

    /**
     * Priorité associée à ce SLA
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priorite priorite;

    /**
     * Catégorie associée à ce SLA
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categorie", nullable = false)
    @JsonIgnoreProperties("slas")
    private Categorie categorie;

    /**
     * Getter utilitaire pour accéder facilement à l'ID
     */
    public Long getId() {
        return this.idSLA;
    }

    @Override
    public String toString() {
        return "SLA{" +
                "idSLA=" + idSLA +
                ", nomSLA='" + nomSLA + '\'' +
                ", delaiResolutionHeure=" + delaiResolutionHeure +
                ", delaiPriseEnChargeHeure=" + delaiPriseEnChargeHeure +
                ", priorite=" + priorite +
                ", categorie=" + (categorie != null ? categorie.getNomCategorie() : "null") +
                '}';
    }
}
