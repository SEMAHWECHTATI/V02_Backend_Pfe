package com.sagem.g2ii.Entity.Authentification;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sagem.g2ii.Entity.Enumeration.TypeAlerte;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "preferencenotification")
@Getter
@Setter
@ToString
@Builder
public class PreferenceNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    private TypeAlerte typeAlerte;

    private boolean canalEmail;

    private boolean canalInApp;

    private boolean actif;

    // RELATION : "recevoir" (1 utilisateur possède 1 préférence)
    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnore
    private Utilisateur utilisateur;
}
