package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.ArticleDTO;
import com.sagem.g2ii.Service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/articles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class ArticleController {

    @Autowired
    private final ArticleService articleService;

    /**
     * ✅ Créer article
     */
    @PostMapping
    public ResponseEntity<?> creerArticle(@RequestBody ArticleDTO dto) {
        try {
            log.info("POST /articles - Création article");
            ArticleDTO created = articleService.creerArticle(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ Article créé avec succès");
            response.put("article", created);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Modifier article
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierArticle(@PathVariable Long id, @RequestBody ArticleDTO dto) {
        try {
            ArticleDTO updated = articleService.modifierArticle(id, dto);
            return ResponseEntity.ok(Map.of("message", "✅ Article modifié", "article", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Récupérer tous les articles
     */
    @GetMapping
    public ResponseEntity<?> getAllArticles() {
        return ResponseEntity.ok(Map.of(
                "total", articleService.getAllArticles().size(),
                "articles", articleService.getAllArticles()
        ));
    }

    /**
     * ✅ Récupérer article par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    /**
     * ✅ Rechercher par référence
     */
    @GetMapping("/reference/{reference}")
    public ResponseEntity<?> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(articleService.getArticleByReference(reference));
    }

    /**
     * ✅ Rechercher par code-barres
     */
    @GetMapping("/barcode/{codeBarres}")
    public ResponseEntity<?> getByCodeBarres(@PathVariable String codeBarres) {
        return ResponseEntity.ok(articleService.getArticleByCodeBarres(codeBarres));
    }

    /**
     * ✅ Articles avec stock faible
     */
    @GetMapping("/stock/faible")
    public ResponseEntity<?> getLowStock() {
        return ResponseEntity.ok(Map.of(
                "total", articleService.getArticlesWithLowStock().size(),
                "articles", articleService.getArticlesWithLowStock()
        ));
    }

    /**
     * ✅ Articles avec stock critique
     */
    @GetMapping("/stock/critique")
    public ResponseEntity<?> getCriticalStock() {
        return ResponseEntity.ok(Map.of(
                "total", articleService.getArticlesWithCriticalStock().size(),
                "articles", articleService.getArticlesWithCriticalStock()
        ));
    }



    /**
     * ✅ Rechercher articles
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String keyword) {
        return ResponseEntity.ok(Map.of(
                "total", articleService.searchArticles(keyword).size(),
                "articles", articleService.searchArticles(keyword)
        ));
    }

    /**
     * ✅ Archiver article
     */
    @DeleteMapping("/{id}/archive")
    public ResponseEntity<?> archiveArticle(@PathVariable Long id) {
        articleService.archiveArticle(id);
        return ResponseEntity.ok(Map.of("message", "✅ Article archivé"));
    }

    /**
     * ✅ Valeur totale inventaire
     */
    @GetMapping("/inventory/valeur-totale")
    public ResponseEntity<?> getTotalValue() {
        try {
            log.info("GET /articles/inventory/valeur-totale");
            BigDecimal total = articleService.getTotalInventoryValue();

            return ResponseEntity.ok(Map.of(
                    "valeurTotale", total,
                    "devise", "EUR",
                    "message", "✅ Valeur totale de l'inventaire"
            ));
        } catch (Exception e) {
            log.error("❌ Erreur calcul valeur totale:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Statistiques détaillées inventaire
     */
    @GetMapping("/inventory/statistiques")
    public ResponseEntity<?> getInventoryStatistics() {
        try {
            log.info("GET /articles/inventory/statistiques");

            BigDecimal valeurTotale = articleService.getTotalInventoryValue();
            Integer totalQuantite = articleService.getTotalQuantityInStock();
            Long totalArticles = articleService.getTotalArticles();

            Map<String, Object> stats = new HashMap<>();
            stats.put("valeurTotale", valeurTotale);
            stats.put("totalQuantite", totalQuantite);
            stats.put("totalArticles", totalArticles);
            stats.put("nombreArticlesFaible", articleService.getArticlesWithLowStock().size());
            stats.put("nombreArticlesCritique", articleService.getArticlesWithCriticalStock().size());
            stats.put("devise", "EUR");

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Erreur statistiques inventaire:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Valeur par type d'article
     */
    @GetMapping("/inventory/valeur-par-type")
    public ResponseEntity<?> getValueByType() {
        try {
            log.info("GET /articles/inventory/valeur-par-type");
            var typeValues = articleService.getValueByType();
            return ResponseEntity.ok(typeValues);
        } catch (Exception e) {
            log.error("❌ Erreur valeur par type:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}