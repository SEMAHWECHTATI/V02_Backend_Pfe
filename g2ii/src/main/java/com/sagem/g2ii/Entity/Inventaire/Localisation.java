package com.sagem.g2ii.Entity.Inventaire;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "localisations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "updated_at")
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "localisation", cascade = CascadeType.ALL)
    private Set<Equipement> equipements = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}