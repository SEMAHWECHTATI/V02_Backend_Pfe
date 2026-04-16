package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
import com.sagem.g2ii.Entity.Enumeration.departementService;
import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private Long id;
    private String token;
    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private String telephone;
    private roleUtilisateur role;
    private departementService departement;
    private String statut;

    // 📌 GROUPES - Important pour le filtrage !
    @JsonProperty("groupes")
    private List<GroupeDTO> groupes;

    // Préférences
    private PreferencesDTO preferences;

    // Dates
    private LocalDateTime dateDernierConnex;
    private LocalDateTime dateCreationCompte;

    // Constructeur pour compatibilité
    public LoginResponse(String token) {
        this.token = token;
    }

    /**
     * DTO interne pour les groupes
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupeDTO {
        private Long id;

        @JsonProperty("nomGroupes")
        private GroupeTechnicien nomGroupes;

        private String description;
        private Boolean actif;
        private LocalDateTime dateCreation;
    }

    /**
     * DTO interne pour les préférences
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PreferencesDTO {
        private Long id;
        private String typeAlerte;
        private Boolean canalEmail;
        private Boolean canalInApp;
        private Boolean actif;
    }
}