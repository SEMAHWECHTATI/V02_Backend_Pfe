package com.sagem.g2ii.DTOs;


import com.sagem.g2ii.Entity.Authentification.DemandeInscription;
import com.sagem.g2ii.Entity.Enumeration.departementService;
import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import jakarta.persistence.Entity;
import lombok.*;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DemandeCreationDTO {

    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private String telephone;
    private departementService departement;
    private String motifDemande;
    private roleUtilisateur roleDemande;

    // L'utilisateur nous envoie juste le numéro du groupe qu'il veut rejoindre
    private Long groupeId;


}
