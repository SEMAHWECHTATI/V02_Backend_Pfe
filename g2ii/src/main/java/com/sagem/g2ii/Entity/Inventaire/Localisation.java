package com.sagem.g2ii.Entity.Inventaire;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "localisations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Localisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String batiment;

    @Column(length = 100)
    private String etage;

    @Column(length = 100)
    private String bureau;

    @Column(length = 100)
    private String armoire;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "localisation")
    @JsonIgnoreProperties("localisation") // 👈 Arrête la boucle infinie ici !
    private List<Equipement> equipements;
}