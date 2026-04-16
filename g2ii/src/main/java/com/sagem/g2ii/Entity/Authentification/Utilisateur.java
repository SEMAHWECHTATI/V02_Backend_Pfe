package com.sagem.g2ii.Entity.Authentification;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sagem.g2ii.Entity.Enumeration.departementService;
import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import com.sagem.g2ii.Entity.Enumeration.statutUtilisateur;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter@Setter
@ToString
@AllArgsConstructor@NoArgsConstructor@Builder
@Table(name = "Utilisateur")

public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 50)
    private String matricule;

    @Column(nullable = false)
    private String motDePasse;

    @Column(length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    private departementService departement;

    @Enumerated(EnumType.STRING)
    private roleUtilisateur role;

    @Column(nullable = false)
    @Builder.Default
    private boolean motDepassetemporaire = false;



    @Column(nullable = false)
    @Builder.Default
    private Integer tentative_login = 0;

    @Column
    private LocalDateTime compteBloqueJusqua;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime date_Creation_Compte = LocalDateTime.now();

    @Column
    @Builder.Default
    private LocalDateTime dateExpmdpTemp = LocalDateTime.now().plusHours(24);
    @PrePersist
    protected void onCreate() {
        if (this.date_Creation_Compte == null) {
            this.date_Creation_Compte = LocalDateTime.now();
        }
        if (this.dateExpmdpTemp == null) {
            this.dateExpmdpTemp = this.date_Creation_Compte.plusHours(24);
        }
    }
    // Un code unique généré pour l'occasion (ex: un UUID ou un code à 6 chiffres)
    private String resetToken;

    // La date limite d'utilisation de ce code (ex: valable 15 minutes)
    private LocalDateTime resetTokenExpiration;

    @Column
    private LocalDateTime date_dernier_Connex;

    @Enumerated(EnumType.STRING)
    private statutUtilisateur statut;

    @ManyToMany
    @JoinTable(
            name = "utilisateur_groupes",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
    private List<Groupe> groupes;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private PreferenceNotification preferences;

    @OneToMany(mappedBy = "demandeur")
    @JsonIgnore// <--- Empêche de remonter au ticket depuis l'utilisateur
    private List<Ticket> ticketsCrees;
// =========================================================================
    // MÉTHODES OBLIGATOIRES DE L'INTERFACE UserDetails POUR SPRING SECURITY
    // =========================================================================

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // On traduit votre rôle (ex: "Administrateur") pour Spring Security
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return this.motDePasse; // On dit à Spring que le mot de passe est ici
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return this.email; // Très important : C'est l'EMAIL qui sert d'identifiant !
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        // Le compte n'est pas verrouillé SI le statut n'est pas "Bloque"
        return this.statut != statutUtilisateur.Bloque;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        // Le compte est activé SI le statut est "Actif"
        return this.statut == statutUtilisateur.Actif;
    }
}
