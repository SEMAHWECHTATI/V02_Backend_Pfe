package com.sagem.g2ii.Entity.Inventaire;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "localisations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 💡 À NE PAS OUBLIER
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

    // 💡 Change le nom dans "name" pour correspondre EXACTEMENT à ta base de données
    @Column(name = "created_at", updatable = false, nullable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime dateCreation;

    // 💡 Si ton autre colonne en BDD s'appelle "updated_at", change-la aussi ici :
    @Column(name = "updated_at")
    @org.hibernate.annotations.UpdateTimestamp
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