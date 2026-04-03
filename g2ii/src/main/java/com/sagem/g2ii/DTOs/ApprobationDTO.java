package com.sagem.g2ii.DTOs;


import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import lombok.Data;

@Data
public class ApprobationDTO {

    // Le rôle définitif que l'Admin décide de donner (ex: "Technicien", "Demandeur", "Admin")
    private roleUtilisateur roleAccorde;

    // L'ID du groupe technique (Obligatoire si c'est un Technicien, peut être vide/null si c'est un Demandeur)
    private Long groupeId;
}
