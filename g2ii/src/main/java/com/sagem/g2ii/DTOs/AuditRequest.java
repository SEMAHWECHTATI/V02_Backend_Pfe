package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Enumeration.TypeAlerte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequest {

    private ModuleAudit module;

    private ActionAudit action;

    private Long entityId;

    private String description;

    private String ancienneValeur;

    private String nouvelleValeur;

    private NiveauAudit niveau;

    private Boolean succes;

    private Utilisateur utilisateur;
}