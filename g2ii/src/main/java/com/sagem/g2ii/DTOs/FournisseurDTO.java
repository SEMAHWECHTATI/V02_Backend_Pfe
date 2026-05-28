package com.sagem.g2ii.DTOs;


import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FournisseurDTO {

    private Long id;

    @NotBlank(message = "Nom fournisseur obligatoire")
    @Size(min = 1, max = 100, message = "Nom entre 1 et 100 caractères")
    private String nom;

    private String contact;

    @Email(message = "Email invalide")
    private String email;

    private String telephone;

    private String adresse;
}
