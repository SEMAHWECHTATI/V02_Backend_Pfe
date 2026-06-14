package com.sagem.g2ii.Controller;


import com.sagem.g2ii.DTOs.DashboardFinanceDTO;
import com.sagem.g2ii.Repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks/dashboard-finance")
@CrossOrigin("*")
public class StockFinanceController {

    @Autowired
    private StockRepository stockRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFinanceStats() {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal valeurGlobale = stockRepository.getValeurGlobaleInventaire();

        // Sécurité si la table est vide (éviter un renvoi null)
        stats.put("valeurGlobale", valeurGlobale != null ? valeurGlobale : BigDecimal.ZERO);
        stats.put("totalStocksFaibles", stockRepository.countStocksFaibles());
        stats.put("totalRuptures", stockRepository.countRupturesCritiques());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardFinanceDTO> getDashboardStats() {
        Map<String, Object> rawData = stockRepository.getFinanceDashboardStats();

        DashboardFinanceDTO dto = DashboardFinanceDTO.builder()
                .valeurGlobale(rawData.get("valeur_globale_parc") != null ? new BigDecimal(rawData.get("valeur_globale_parc").toString()) : BigDecimal.ZERO)
                .totalStocksFaibles(rawData.get("total_stocks_faibles") != null ? ((Number) rawData.get("total_stocks_faibles")).longValue() : 0L)
                .totalRuptures(rawData.get("total_ruptures") != null ? ((Number) rawData.get("total_ruptures")).longValue() : 0L)
                .build();

        return ResponseEntity.ok(dto);
    }
}