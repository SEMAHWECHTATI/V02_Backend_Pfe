package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Repository.IntGroupe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupeService {

    private final IntGroupe grouperepo;
    private final JournalAuditService journalAuditService; // 🌟 Injection du service d'audit

    /**
     * Helper pour extraire l'utilisateur connecté depuis le SecurityContext de Spring Security
     */
    private Utilisateur getConnectedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Utilisateur) {
                return (Utilisateur) principal;
            }
        } catch (Exception e) {
            // Pas de contexte de session web (ex: tâches de fond ou contextes déconnectés)
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Groupe getGroupeById(Long id){
        return grouperepo.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Groupe> getAllGroupe() {
        return grouperepo.findAll();
    }

    @Transactional(readOnly = true)
    public Groupe getById(Long id) {
        return grouperepo.findById(id).orElse(null);
    }

    @Transactional
    public Groupe ajouterGroup(Groupe groupe){
        groupe.setDateCreation(LocalDateTime.now());
        Groupe savedGroupe = grouperepo.save(groupe);

        log.info("✅ Groupe créé avec succès : {}", savedGroupe.getNomGroupes());

        // 🌟 LOG D'AUDIT : Création du groupe technique / rôle d'accès
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.UTILISATEUR, // 🎯 Catégorisé sous la gestion utilisateur/droits
                ActionAudit.APPROBATION_DEMANDE,
                "Groupe",
                savedGroupe.getId(),
                "Création manuelle d'un nouveau groupe d'utilisateurs : " + savedGroupe.getNomGroupes(),
                null,
                String.format("{nomGroupe: '%s', actif: %b}", savedGroupe.getNomGroupes(), savedGroupe.isActif()),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        return savedGroupe;
    }

    @Transactional
    public void deleteGroupe(Long id) {
        log.info("🗑️ Tentative suppression du Groupe ID = {}", id);

        Groupe groupe = grouperepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        String nomGroupeHistorique = String.valueOf(groupe.getNomGroupes());
        String historiqueSuppression = String.format("{id: %d, nomGroupe: '%s'}", groupe.getId(), nomGroupeHistorique);

        grouperepo.delete(groupe);
        log.info("✅ Groupe '{}' supprimé avec succès", nomGroupeHistorique);

        // 🌟 LOG D'AUDIT : Suppression définitive d'un groupe (Niveau WARNING)
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.UTILISATEUR,
                ActionAudit.BLOCAGE, // Utilisation de BLOCAGE pour marquer la suppression/retrait de droits
                "Groupe",
                id,
                "Suppression définitive du groupe d'utilisateurs : " + nomGroupeHistorique,
                historiqueSuppression,
                null,
                NiveauAudit.WARNING,
                true,
                getConnectedUser()
        );
    }
}