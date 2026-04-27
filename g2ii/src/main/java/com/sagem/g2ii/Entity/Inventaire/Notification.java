package com.sagem.g2ii.Entity.Inventaire;

import com.sagem.g2ii.Entity.Enumeration.CanalNotification;
import com.sagem.g2ii.Entity.Enumeration.StatutNotification;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotification canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutNotification statut = StatutNotification.EN_ATTENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateEnvoi;

    private LocalDateTime dateLecture;

    @ManyToOne
    @JoinColumn(name = "alerte_id")
    private Alerte alerte;

    @PrePersist
    public void prePersist() {
        dateCreation = LocalDateTime.now();
    }
}