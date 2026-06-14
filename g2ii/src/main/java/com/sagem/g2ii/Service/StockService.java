package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.StockDTO;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Stock;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockService {

    private final StockRepository stockRepository;
    private final ArticleRepository articleRepository;
    private final AlerteService alerteService;

    @Transactional(readOnly = true)
    public List<StockDTO> recupererToutLeStock() {
        return stockRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Créer un stock pour un article
     */
    public StockDTO creerStock(Long articleId, StockDTO dto) {
        log.info("📊 Création stock pour article: {}", articleId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Stock stock = Stock.builder()
                .article(article)
                .codeBarresArticle(dto.getCodeBarresArticle())
                .quantiteEnStock(dto.getQuantiteEnStock())
                .quantiteCritique(dto.getQuantiteCritique())
                .quantiteMinimum(dto.getQuantiteMinimum())
                .prixUnitaire(dto.getPrixUnitaire())
                .build();

        Stock saved = stockRepository.save(stock);
        log.info("✅ Stock créé: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public void mettreAJourLocalisation(Long stockId, String nouvelleLocalisation) {
        log.info("Mise à jour de la localisation du stock ID {}: -> {}", stockId, nouvelleLocalisation);

        // 1. Récupérer le stock
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Impossible de mettre à jour la localisation : Stock non trouvé avec l'ID " + stockId));

        // 2. Mettre à jour le champ de localisation de l'entité Stock
        // 💡 Adaptez le setter selon le nom exact du champ dans votre entité Stock (ex: setEmplacement, setZone, etc.)

        // 3. Sauvegarder les modifications
        stockRepository.save(stock);
        log.info("✅ Localisation du stock ID {} mise à jour avec succès.", stockId);
    }



    /**
     * ✅ Mettre à jour la quantité
     */
    public void mettreAJourQuantite(Long stockId, Integer nouvelleQuantite) {
        log.info("🔄 Mise à jour quantité stock: {}", stockId);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        Integer ancienneQuantite = stock.getQuantiteEnStock();
        stock.setQuantiteEnStock(nouvelleQuantite);
        stockRepository.save(stock);

        // Mettre à jour l'article aussi
        Article article = stock.getArticle();
        article.setQuantiteEnStock(nouvelleQuantite);
        articleRepository.save(article);

        log.info("✅ Quantité mise à jour: {} -> {}", ancienneQuantite, nouvelleQuantite);

        // Vérifier les alertes
        if (nouvelleQuantite <= stock.getQuantiteCritique()) {
            alerteService.creerAlerte(article, "Stock critique!");
        }
    }

    /**
     * ✅ Récupérer stock faible
     */
    public List<StockDTO> getStockFaible() {
        return stockRepository.findStockFaible().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔄 Convertir Entity en DTO
     */
    public StockDTO convertToDTO(Stock stock) {
        if (stock == null) return null;

        BigDecimal valeurTotale = BigDecimal.ZERO;
        if (stock.getPrixUnitaire() != null && stock.getQuantiteEnStock() != null) {
            valeurTotale = stock.getPrixUnitaire()
                    .multiply(new BigDecimal(stock.getQuantiteEnStock()));
        }

        return StockDTO.builder()
                .id(stock.getId())
                .codeBarresArticle(stock.getCodeBarresArticle())
                .quantiteEnStock(stock.getQuantiteEnStock())
                .quantiteCritique(stock.getQuantiteCritique())
                .quantiteMinimum(stock.getQuantiteMinimum())
                .prixUnitaire(stock.getPrixUnitaire())  // ✅ Maintenant BigDecimal
                // Relations
                .articleId(stock.getArticle() != null ? stock.getArticle().getId() : null)
                .articleReference(stock.getArticle() != null ? stock.getArticle().getReference() : null)
                .articleDesignation(stock.getArticle() != null ? stock.getArticle().getDesignation() : null)
                .articleTypeArticle(stock.getArticle() != null ? stock.getArticle().getTypeArticle().getLabel() : null)
                .articleStatut(stock.getArticle() != null ? stock.getArticle().getStatut().getLabel() : null)
                // Informations calculées
                .valeurTotale(valeurTotale)  // ✅ Maintenant BigDecimal
                .estFaible(stock.getQuantiteEnStock() != null && stock.getQuantiteMinimum() != null &&
                        stock.getQuantiteEnStock() <= stock.getQuantiteMinimum())
                .estCritique(stock.getQuantiteEnStock() != null && stock.getQuantiteCritique() != null &&
                        stock.getQuantiteEnStock() <= stock.getQuantiteCritique())
                .build();
    }
}
