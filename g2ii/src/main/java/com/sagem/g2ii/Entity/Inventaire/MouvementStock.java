package com.sagem.g2ii.Entity.Inventaire;

import com.sagem.g2ii.Entity.Enumeration.TypeMouvement;
import jakarta.persistence.*;
import lombok.*;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement type;

    @Column(nullable = false)
    private Integer quantite;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(length = 200)
    private String localisationSource;

    @Column(length = 200)
    private String localisationDestination;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateMouvement;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur responsable;

    @Column(length = 100)
    private String referenceTicket; // Lien avec les tickets

    @PrePersist
    public void prePersist() {
        dateMouvement = LocalDateTime.now();
    }
}