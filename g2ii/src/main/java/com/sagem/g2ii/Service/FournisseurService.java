package com.sagem.g2ii.Service;


import com.sagem.g2ii.DTOs.FournisseurDTO;
import com.sagem.g2ii.Entity.Inventaire.Fournisseur;
import com.sagem.g2ii.Repository.FournisseurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;

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
        // Optionnel : Vérifier si le nom existe déjà si la règle "unique = true" est stricte
        if (fournisseurRepository.existsByNomIgnoreCase(dto.getNom())) {
            throw new IllegalArgumentException("❌ Un fournisseur avec ce nom existe déjà.");
        }

        Fournisseur fournisseur = convertToEntity(dto);
        // La date de création est gérée automatiquement par ton @PrePersist
        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);

        log.info("✅ Fournisseur créé : {}", savedFournisseur.getNom());
        return convertToDTO(savedFournisseur);
    }

    @Transactional
    public FournisseurDTO updateFournisseur(Long id, FournisseurDTO dto) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Impossible de modifier. Fournisseur introuvable."));

        // Mise à jour des informations
        fournisseur.setNom(dto.getNom());
        fournisseur.setContact(dto.getContact());
        fournisseur.setEmail(dto.getEmail());
        fournisseur.setTelephone(dto.getTelephone());
        fournisseur.setAdresse(dto.getAdresse());

        Fournisseur updatedFournisseur = fournisseurRepository.save(fournisseur);
        log.info("✅ Fournisseur mis à jour : {}", updatedFournisseur.getNom());
        return convertToDTO(updatedFournisseur);
    }

    @Transactional
    public void deleteFournisseur(Long id) {
        if (!fournisseurRepository.existsById(id)) {
            throw new EntityNotFoundException("❌ Impossible de supprimer. Fournisseur introuvable.");
        }
        fournisseurRepository.deleteById(id);
        log.info("✅ Fournisseur supprimé avec l'ID : {}", id);
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