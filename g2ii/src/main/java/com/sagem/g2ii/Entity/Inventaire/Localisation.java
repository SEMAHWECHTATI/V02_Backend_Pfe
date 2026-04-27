package com.sagem.g2ii.Entity.Inventaire;

import com.sagem.g2ii.Entity.Inventaire.Article;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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
    private String batiment;

    @Column(length = 50)
    private String etage;

    @Column(length = 100)
    private String bureau;

    @Column(length = 100)
    private String departement;

    @OneToMany(mappedBy = "localisation", cascade = CascadeType.ALL)
    private List<Article> articles;

    @Column(updatable = false)
    private java.time.LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        dateCreation = java.time.LocalDateTime.now();
    }
}