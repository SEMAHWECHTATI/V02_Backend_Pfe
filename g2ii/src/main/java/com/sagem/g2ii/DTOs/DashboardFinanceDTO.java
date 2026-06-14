package com.sagem.g2ii.DTOs;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardFinanceDTO {
    private BigDecimal valeurGlobale;
    private Long totalStocksFaibles;
    private Long totalRuptures;
}