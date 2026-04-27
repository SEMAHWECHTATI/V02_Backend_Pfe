package com.sagem.g2ii.DTOs;

import com.sagem.g2ii.Entity.Enumeration.Severite;
import com.sagem.g2ii.Entity.Enumeration.StatutAlerte;
import com.sagem.g2ii.Entity.Enumeration.TypeAlerte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteDTO {
    private Long id;
    private TypeAlerte type;
    private String message;
    private Severite severite;
    private StatutAlerte statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateAcquittement;
    private List<Long> articleIds;
    private List<String> articleDesignations;
}
