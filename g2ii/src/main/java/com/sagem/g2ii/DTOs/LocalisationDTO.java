package com.sagem.g2ii.DTOs;


import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalisationDTO {

    private Long id;

    @NotBlank(message = "Nom localisation obligatoire")
    @Size(min = 1, max = 100, message = "Nom entre 1 et 100 caractères")
    private String nom;

    private String description;

    private String batiment;

    private String etage;

    private String bureau;

    private String armoire;

    private boolean active;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;
}
