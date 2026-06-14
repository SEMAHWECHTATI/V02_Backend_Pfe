package com.sagem.g2ii.DTOs;


import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Inventaire.Equipement;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipementDTO {

    private Long id;

    @NotBlank(message = "Numéro de série obligatoire")
    @Size(min = 1, max = 100, message = "Numéro de série entre 1 et 100 caractères")
    private String numeroSerie;

    @NotBlank(message = "Désignation obligatoire")
    @Size(min = 1, max = 255, message = "Désignation entre 1 et 255 caractères")
    private String designation;

    @NotNull(message = "Article obligatoire")
    private Long articleId;

    private String articleDesignation;

    @NotNull(message = "Statut obligatoire")
    private StatutArticle statut;

    private Long localisationId;

    private String localisationNom;

    private String observations;

    private LocalDateTime dateAcquisition;

    private LocalDateTime dateMiseAuRebut;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    private String creePar;

    private Long responsableId;

    private String responsableNom;
}
