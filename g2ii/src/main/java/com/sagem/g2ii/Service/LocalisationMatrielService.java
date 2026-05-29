package com.sagem.g2ii.Service;


import com.sagem.g2ii.DTOs.LocalisationDTO;
import com.sagem.g2ii.Entity.Inventaire.Localisation;
import com.sagem.g2ii.Repository.LocalisationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocalisationMatrielService {


    private final LocalisationRepository localisationRepository;

    public List<LocalisationDTO> getAllLocalisations() {
        return localisationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LocalisationDTO getLocalisationById(Long id) {
        Localisation localisation = localisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Localisation introuvable avec l'ID : " + id));
        return this.convertToDTO(localisation);
    }

    /**
     * Crée une nouvelle localisation
     */
    public LocalisationDTO creerLocalisation(LocalisationDTO localisationDTO) {
        Localisation localisation = convertToEntity(localisationDTO);
        localisation.setActive(true); // Active par défaut à la création

        // 💡 PLUS BESOIN DE : localisation.setDateCreation(LocalDateTime.now());

        Localisation saved = localisationRepository.save(localisation);
        return convertToDTO(saved); // Utiliser 'saved' et non 'localisation'
    }

    /**
     * Met à jour une localisation existante
     */
    public LocalisationDTO modifierLocalisation(Long id, LocalisationDTO dto) {
        Localisation localisation = localisationRepository.findById(id)
                // 💡 Remplacement par une exception plus standard
                .orElseThrow(() -> new EntityNotFoundException("❌ Impossible de modifier. Localisation introuvable."));

        localisation.setNom(dto.getNom());
        localisation.setBatiment(dto.getBatiment());
        localisation.setEtage(dto.getEtage());
        localisation.setBureau(dto.getBureau());
        localisation.setArmoire(dto.getArmoire());
        localisation.setDescription(dto.getDescription());
        localisation.setActive(dto.isActive());

        // 💡 PLUS BESOIN DE : localisation.setDateModification(LocalDateTime.now());

        Localisation updated = localisationRepository.save(localisation);
        return this.convertToDTO(updated);
    }

    /**
     * Suppression logique (désactivation) ou physique d'une localisation
     */
    @Transactional
    public void supprimerLocalisation(Long id) {
        if (!localisationRepository.existsById(id)) {
            throw new RuntimeException("❌ Impossible de supprimer. Localisation introuvable.");
        }
        localisationRepository.deleteById(id);
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
        // Les dates sont gérées directement à l'injection/modification
        return localisation;
    }
}
