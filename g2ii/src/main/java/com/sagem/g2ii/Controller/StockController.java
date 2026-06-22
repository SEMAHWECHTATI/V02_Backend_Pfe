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
    /**
     * 🔍 Récupérer l'état du stock lié à un article spécifique
     * GET http://localhost:8070/api/inventory/stocks/article/{articleId}
     */
    @GetMapping("/article/{articleId}") // 🎯 AJOUT DE CETTE ANNOTATION GET
    public ResponseEntity<?> getStockByArticleId(@PathVariable Long articleId) {
        try {
            log.info("📥 Requête de récupération du stock pour l'article ID: {}", articleId);
            StockDTO stockDTO = stockService.recupererStockParArticle(articleId);
            return ResponseEntity.ok(stockDTO);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

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
     * 📉 Diminuer la quantité suite à la résolution d'un ticket (Appelé par Angular)
     * PUT http://localhost:8070/api/inventory/stocks/{stockId}/diminuer?quantite=2
     */
    @PutMapping("/{stockId}/diminuer")
    public ResponseEntity<?> diminuerQuantite(
            @PathVariable Long stockId,
            @RequestParam Integer quantite) {
        try {
            log.info("📥 Décompte de stock - Stock ID: {}, Quantité retirée: -{}", stockId, quantite);
            stockService.diminuerQuantite(stockId, quantite); // Méthode de soustraction (stock.quantite - quantite)
            return ResponseEntity.ok(Map.of("message", "✅ Pièce décomptée du stock avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la diminution du stock: {}", e.getMessage());
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

    /**
     * 🗑️ Supprimer définitivement une ligne de stock
     * DELETE http://localhost:8070/api/inventory/stocks/{stockId}
     */
    @DeleteMapping("/{stockId}") // 🎯 AJOUT DE CET ENDPOINT POUR SUPPRIMER LE STOCK
    public ResponseEntity<?> supprimerStock(@PathVariable Long stockId) {
        try {
            log.info("🗑️ Requête de suppression définitive pour le stock ID: {}", stockId);
            stockService.supprimerStock(stockId); // Adaptez le nom selon votre service
            return ResponseEntity.ok(Map.of("message", "✅ Ligne de stock supprimée avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}