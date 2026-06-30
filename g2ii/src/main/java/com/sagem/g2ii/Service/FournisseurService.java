package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.FournisseurDTO;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Inventaire.Fournisseur;
import com.sagem.g2ii.Repository.FournisseurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;
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
    public List<FournisseurDTO> getAllFournisseurs() {
        return fournisseurRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FournisseurDTO getFournisseurById(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Fournisseur introuvable avec l'ID : " + id));
        return convertToDTO(fournisseur);
    }

    @Transactional
    public FournisseurDTO createFournisseur(FournisseurDTO dto) {
        if (fournisseurRepository.existsByNomIgnoreCase(dto.getNom())) {
            throw new IllegalArgumentException("❌ Un fournisseur avec ce nom existe déjà.");
        }

        Fournisseur fournisseur = convertToEntity(dto);
        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);
        log.info("✅ Fournisseur créé : {}", savedFournisseur.getNom());

        // 🌟 LOG D'AUDIT : Création du fournisseur
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.APPROBATION_DEMANDE, // Enregistrement d'un nouvel acteur du stock
                "Fournisseur",
                savedFournisseur.getId(),
                "Création manuelle du fournisseur : " + savedFournisseur.getNom(),
                null,
                String.format("{nom: '%s', email: '%s', contact: '%s'}", savedFournisseur.getNom(), savedFournisseur.getEmail(), savedFournisseur.getContact()),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        return convertToDTO(savedFournisseur);
    }

    @Transactional
    public FournisseurDTO updateFournisseur(Long id, FournisseurDTO dto) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Impossible de modifier. Fournisseur introuvable."));

        // Capture de l'ancien état pour l'historique d'audit
        String ancienneValeurStr = String.format("{nom: '%s', email: '%s', telephone: '%s'}",
                fournisseur.getNom(), fournisseur.getEmail(), fournisseur.getTelephone());

        // Mise à jour des informations
        fournisseur.setNom(dto.getNom());
        fournisseur.setContact(dto.getContact());
        fournisseur.setEmail(dto.getEmail());
        fournisseur.setTelephone(dto.getTelephone());
        fournisseur.setAdresse(dto.getAdresse());

        Fournisseur updatedFournisseur = fournisseurRepository.save(fournisseur);
        log.info("✅ Fournisseur mis à jour : {}", updatedFournisseur.getNom());

        String nouvelleValeurStr = String.format("{nom: '%s', email: '%s', telephone: '%s'}",
                updatedFournisseur.getNom(), updatedFournisseur.getEmail(), updatedFournisseur.getTelephone());

        // 🌟 LOG D'AUDIT : Modification du fournisseur
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.CHANGEMENT_MDP, // Sens générique de modification / mise à jour
                "Fournisseur",
                updatedFournisseur.getId(),
                "Mise à jour des coordonnées du fournisseur : " + updatedFournisseur.getNom(),
                ancienneValeurStr,
                nouvelleValeurStr,
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        return convertToDTO(updatedFournisseur);
    }

    @Transactional
    public void deleteFournisseur(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Impossible de supprimer. Fournisseur introuvable."));

        // Sauvegarde des informations avant suppression définitive
        String historiqueSuppression = String.format("{id: %d, nom: '%s', email: '%s'}",
                fournisseur.getId(), fournisseur.getNom(), fournisseur.getEmail());

        fournisseurRepository.delete(fournisseur); // Remplacement de deleteById par delete pour garantir la cohérence de l'objet récupéré
        log.info("✅ Fournisseur supprimé avec l'ID : {}", id);

        // 🌟 LOG D'AUDIT : Suppression définitive (Marqué avec action BLOCAGE / Attention Warning)
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.BLOCAGE,
                "Fournisseur",
                id,
                "Suppression définitive du fournisseur de la base de données : " + fournisseur.getNom(),
                historiqueSuppression,
                null,
                NiveauAudit.WARNING,
                true,
                getConnectedUser()
        );
    }

    /* =========================================================================
       📦 MAPPERS (DTO <-> ENTITY)
       ========================================================================= */

    private FournisseurDTO convertToDTO(Fournisseur fournisseur) {
        if (fournisseur == null) return null;

        return FournisseurDTO.builder()
                .id(fournisseur.getId())
                .nom(fournisseur.getNom())
                .contact(fournisseur.getContact())
                .email(fournisseur.getEmail())
                .telephone(fournisseur.getTelephone())
                .adresse(fournisseur.getAdresse())
                .build();
    }

    private Fournisseur convertToEntity(FournisseurDTO dto) {
        if (dto == null) return null;

        return Fournisseur.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .contact(dto.getContact())
                .email(dto.getEmail())
                .telephone(dto.getTelephone())
                .adresse(dto.getAdresse())
                .build();
    }
}