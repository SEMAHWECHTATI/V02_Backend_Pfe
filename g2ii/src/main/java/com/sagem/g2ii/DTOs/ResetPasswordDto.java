package com.sagem.g2ii.DTOs;

import lombok.Data;

@Data
public class ResetPasswordDto {

    private String token;
    private String nouveauMotDePasse;
}
