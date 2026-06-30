package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.LocalisationDTO;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Inventaire.Localisation;
import com.sagem.g2ii.Repository.LocalisationRepository;
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
@Transactional
public class LocalisationMatrielService {

    private final LocalisationRepository localisationRepository;
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
            // Pas de contexte de session actif (ex: script d'initialisation ou tâche système)
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<LocalisationDTO> getAllLocalisations() {
        return localisationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocalisationDTO getLocalisationById(Long id) {
        Localisation localisation = localisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Localisation introuvable avec l'ID : " + id));
        return this.convertToDTO(localisation);
    }

    /**
     * Crée une nouvelle localisation
     */
    public LocalisationDTO creerLocalisation(LocalisationDTO localisationDTO) {
        Localisation localisation = convertToEntity(localisationDTO);
        localisation.setActive(true); // Active par défaut à la création

        Localisation saved = localisationRepository.save(localisation);
        log.info("✅ Localisation créée : {}", saved.getNom());

        // 🌟 LOG D'AUDIT : Création d'une localisation de matériel
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.APPROBATION_DEMANDE,
                "Localisation",
                saved.getId(),
                "Création d'une nouvelle zone de stockage / localisation : " + saved.getNom(),
                null,
                String.format("{nom: '%s', batiment: '%s', etage: '%s', bureau: '%s'}",
                        saved.getNom(), saved.getBatiment(), saved.getEtage(), saved.getBureau()),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        return convertToDTO(saved);
    }

    /**
     * Met à jour une localisation existante
     */
    public LocalisationDTO modifierLocalisation(Long id, LocalisationDTO dto) {
        Localisation localisation = localisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Impossible de modifier. Localisation introuvable."));

        // Capture de l'ancien état avant écrasement
        String ancienneValeurStr = String.format("{nom: '%s', batiment: '%s', bureau: '%s', active: %b}",
                localisation.getNom(), localisation.getBatiment(), localisation.getBureau(), localisation.isActive());

        localisation.setNom(dto.getNom());
        localisation.setBatiment(dto.getBatiment());
        localisation.setEtage(dto.getEtage());
        localisation.setBureau(dto.getBureau());
        localisation.setArmoire(dto.getArmoire());
        localisation.setDescription(dto.getDescription());
        localisation.setActive(dto.isActive());

        Localisation updated = localisationRepository.save(localisation);
        log.info("✅ Localisation mise à jour : {}", updated.getNom());

        String nouvelleValeurStr = String.format("{nom: '%s', batiment: '%s', bureau: '%s', active: %b}",
                updated.getNom(), updated.getBatiment(), updated.getBureau(), updated.isActive());

        // 🌟 LOG D'AUDIT : Modification de la localisation
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.CHANGEMENT_MDP, // Changement/Mise à jour d'attributs
                "Localisation",
                updated.getId(),
                "Mise à jour des coordonnées de la localisation : " + updated.getNom(),
                ancienneValeurStr,
                nouvelleValeurStr,
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        return this.convertToDTO(updated);
    }

    /**
     * Suppression physique d'une localisation
     */
    public void supprimerLocalisation(Long id) {
        Localisation localisation = localisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Impossible de supprimer. Localisation introuvable."));

        String historiqueSuppression = String.format("{id: %d, nom: '%s', batiment: '%s'}",
                localisation.getId(), localisation.getNom(), localisation.getBatiment());

        localisationRepository.delete(localisation);
        log.info("✅ Localisation ID : {} supprimée définitivement.", id);

        // 🌟 LOG D'AUDIT : Trace de la suppression physique (Marqué en niveau WARNING)
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.BLOCAGE, // Rétractation / Retrait de la zone en base
                "Localisation",
                id,
                "Suppression définitive de l'emplacement matériel : " + localisation.getNom(),
                historiqueSuppression,
                null,
                NiveauAudit.WARNING,
                true,
                getConnectedUser()
        );
    }

    /* =========================================================================
       📦 MAPPERS / CONVERTISSEURS (DTO <-> ENTITY)
       ========================================================================= */

    private LocalisationDTO convertToDTO(Localisation localisation) {
        if (localisation == null) return null;

        return LocalisationDTO.builder()
                .id(localisation.getId())
                .nom(localisation.getNom())
                .etage(localisation.getEtage())
                .armoire(localisation.getArmoire())
                .description(localisation.getDescription())
                .batiment(localisation.getBatiment())
                .bureau(localisation.getBureau())
                .dateCreation(localisation.getDateCreation())
                .dateModification(localisation.getDateModification())
                .active(localisation.isActive())
                .build();
    }

    private Localisation convertToEntity(LocalisationDTO dto) {
        if (dto == null) return null;

        Localisation localisation = new Localisation();
        localisation.setId(dto.getId());
        localisation.setNom(dto.getNom());
        localisation.setBatiment(dto.getBatiment());
        localisation.setEtage(dto.getEtage());
        localisation.setBureau(dto.getBureau());
        localisation.setArmoire(dto.getArmoire());
        localisation.setDescription(dto.getDescription());
        localisation.setActive(dto.isActive());
        return localisation;
    }
}