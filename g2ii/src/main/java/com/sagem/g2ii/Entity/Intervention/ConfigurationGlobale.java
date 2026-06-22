package com.sagem.g2ii.Entity.Intervention;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuration_globale")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigurationGlobale {

    @Id
    private Long id = 1L; // Ligne unique

    @Column(nullable = false)
    private double indiceFaisabiliteEquipe;

    @Column(nullable = false)
    private boolean alertesEmailActives;

    @Column(nullable = false)
    private boolean autoAssignationActive;
}