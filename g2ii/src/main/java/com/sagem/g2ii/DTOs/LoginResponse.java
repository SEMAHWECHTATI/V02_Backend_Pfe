package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.departementService;
import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private String nom;
    private String prenom;
    private roleUtilisateur role; // Type exact selon votre erreur
    private departementService departement; // Type exact selon votre erreur
    private String email;

    // --- AJOUTEZ CECI ---
    // Constructeur pour que l'ancien code d'AuthentificationService fonctionne toujours
    public LoginResponse(String token) {
        this.token = token;
    }
}
