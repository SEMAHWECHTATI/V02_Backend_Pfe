package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.StockDTO;
import com.sagem.g2ii.Service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/stocks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class StockController {

    private final StockService stockService;

    /**
     * 📊 Créer une entrée de stock pour un article spécifique
     * POST http://localhost:8070/api/inventory/stocks/article/{articleId}
     */
    @PostMapping("/article/{articleId}")
    public ResponseEntity<?> creerStock(
            @PathVariable Long articleId,
            @RequestBody StockDTO stockDTO) {
        try {
            log.info("Requête de création de stock pour l'article ID: {}", articleId);
            StockDTO nouveauStock = stockService.creerStock(articleId, stockDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouveauStock);
        } catch (Exception e) {
            log.error("Erreur lors de la création du stock: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔄 Mettre à jour manuellement ou directement la quantité d'un stock
     * PUT http://localhost:8070/api/inventory/stocks/{stockId}/quantite
     */
    @PutMapping("/{stockId}/quantite")
    public ResponseEntity<?> mettreAJourQuantite(
            @PathVariable Long stockId,
            @RequestParam Integer nouvelleQuantite) {
        try {
            log.info("Requête d'ajustement de quantité pour le stock ID: {} -> {}", stockId, nouvelleQuantite);
            stockService.mettreAJourQuantite(stockId, nouvelleQuantite);
            return ResponseEntity.ok(Map.of("message", "✅ Quantité mise à jour et alertes vérifiées"));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la quantité: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ⚠️ Récupérer tous les stocks en état de faiblesse (Quantité en stock <= Quantité minimum)
     * GET http://localhost:8070/api/inventory/stocks/faibles
     */
    @GetMapping("/faibles")
    public ResponseEntity<List<StockDTO>> getStockFaible() {
        log.info("Extraction de la liste des stocks faibles");
        List<StockDTO> stocksFaibles = stockService.getStockFaible();
        return ResponseEntity.ok(stocksFaibles);
    }

    /**
     * ✅ GET /api/inventory/stocks
     * Récupérer l'intégralité du stock physique (Demande Angular)
     */
    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        log.info("📥 GET /api/inventory/stocks - Récupération globale du stock physique");
        return ResponseEntity.ok(stockService.recupererToutLeStock());
    }
}