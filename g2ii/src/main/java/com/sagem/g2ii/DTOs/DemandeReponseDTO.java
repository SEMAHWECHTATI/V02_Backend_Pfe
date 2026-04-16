package com.sagem.g2ii.DTOs;


import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
import com.sagem.g2ii.Entity.Enumeration.departementService;
import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import com.sagem.g2ii.Entity.Enumeration.statutDemandeInscription;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DemandeReponseDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String motifDemande;
    private String matricule;
    private String telephone;
    private departementService departement;
    private roleUtilisateur roleDemande;
    private statutDemandeInscription statut;
    private LocalDateTime dateDemande;
    private GroupeTechnicien nomGroupe; // On renvoie juste le nom du groupe, c'est plus propre !
}
