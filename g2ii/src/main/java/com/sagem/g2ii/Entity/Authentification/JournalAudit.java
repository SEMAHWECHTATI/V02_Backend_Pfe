package com.sagem.g2ii.Entity.Authentification;

import com.sagem.g2ii.Entity.Enumeration.ActionAudit; // Tu dois créer cet Enum
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionAudit action; // Exemple: CONNEXION, APPROBATION_INSCRIPTION, MODIFICATION

    @Column(columnDefinition = "TEXT")
    private String description;

    private String adresseIp;

    private LocalDateTime dateAction;

    // Optionnel mais recommandé : Savoir quel utilisateur a fait l'action
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
}