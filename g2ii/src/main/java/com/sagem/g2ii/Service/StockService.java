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
     * ✅ Créer un stock pour un article ET diminuer sa quantité dans le catalogue
     */
    @Transactional // 🎯 TRÈS IMPORTANT : Garantit que l'article et le stock se mettent à jour ensemble
    public StockDTO creerStock(Long articleId, StockDTO dto) {
        log.info("📊 Création stock pour article: {}", articleId);

        // 1. Récupération de l'article depuis la table 'articles'
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        int quantiteATransferer = dto.getQuantiteEnStock();

        // 2. 🎯 SÉCURITÉ : On vérifie si l'article possède assez de quantité physique à transférer
        // (Vérifiez si le champ dans votre entité Article s'appelle bien getQuantite() ou getQuantiteDisponible())
        if (article.getQuantiteEnStock() < quantiteATransferer) {
            throw new IllegalArgumentException("Quantité insuffisante dans le catalogue d'articles. Disponible : "
                    + article.getQuantiteEnStock() + ", Demandé : " + quantiteATransferer);
        }

        // 3. 🎯 DIMINUTION : On soustrait la quantité de l'article et on sauvegarde l'article
        int nouvelleQuantiteCatalogue = article.getQuantiteEnStock() - quantiteATransferer;
        article.setQuantiteEnStock(nouvelleQuantiteCatalogue);
        articleRepository.save(article);
        log.info("📉 Article ID {} mis à jour dans le catalogue. Ancienne qté -> Nouvelle qté : {}", articleId, nouvelleQuantiteCatalogue);

        // 4. Création de la ligne dans la table 'stocks'
        Stock stock = Stock.builder()
                .article(article)
                .codeBarresArticle(dto.getCodeBarresArticle())
                .quantiteEnStock(quantiteATransferer) // Reçoit la quantité transférée
                .quantiteCritique(dto.getQuantiteCritique())
                .quantiteMinimum(dto.getQuantiteMinimum())
                .prixUnitaire(dto.getPrixUnitaire())
                .build();

        Stock saved = stockRepository.save(stock);
        log.info("✅ Stock créé avec ID: {}", saved.getId());

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

    @Transactional
    public void supprimerStock(Long stockId) {
        // 1. On vérifie si la ligne de stock existe avant de tenter la suppression
        if (!stockRepository.existsById(stockId)) {
            throw new RuntimeException("Impossible de supprimer : aucun stock trouvé avec l'ID " + stockId);
        }

        // 2. Suppression physique (Attention : à cause de cascade = CascadeType.ALL,
        // cela supprimera aussi l'historique des MouvementStock liés à ce stock !)
        stockRepository.deleteById(stockId);
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
     * 🔍 Récupérer le stock associé à un article (Requis pour l'appel GET de votre Controller)
     */
    @Transactional(readOnly = true)
    public StockDTO recupererStockParArticle(Long articleId) {
        log.info("🔍 Recherche du stock pour l'article ID: {}", articleId);

        // Recherche le stock lié à l'article.
        // Si findByArticleId n'est pas dans votre Repository, utilisez findByArticle_Id(articleId)
        Stock stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour l'article ID: " + articleId));

        return convertToDTO(stock);
    }

    /**
     * 📉 Diminuer la quantité en stock lors de la consommation d'une pièce sur un ticket
     */
    @Transactional
    public void diminuerQuantite(Long stockId, Integer quantiteADeduire) {
        log.info("📉 Diminution du stock ID {} de la quantité : -{}", stockId, quantiteADeduire);

        // 1. Récupération de la ligne de stock physique
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        if (stock.getQuantiteEnStock() == null) {
            stock.setQuantiteEnStock(0);
        }

        // 2. Calcul et vérification de la nouvelle quantité physique
        int nouvelleQuantite = stock.getQuantiteEnStock() - quantiteADeduire;
        if (nouvelleQuantite < 0) {
            // Sécurité : Interdire la consommation si on n'a pas assez de pièces en rayon
            throw new RuntimeException("Erreur : Stock insuffisant en magasin ! Quantité disponible : " + stock.getQuantiteEnStock());
        }

        // 3. Mise à jour de l'entité STOCK UNIQUEMENT (On touche pas à l'entité Article)
        stock.setQuantiteEnStock(nouvelleQuantite);
        stockRepository.save(stock);
        log.info("✅ Table STOCK mise à jour. Nouvelle quantité physique: {}", nouvelleQuantite);

        // 4. 🎯 Déclenchement intelligent de l'alerte si le stock passe sous les seuils de l'article
        Article article = stock.getArticle();
        if (article != null) {

            // On compare la quantité restante dans le STOCK face au seuil minimum défini dans l'ARTICLE
            if (nouvelleQuantite <= article.getSeuilMinimum()) {

                // On prépare un message clair pour le technicien ou gestionnaire
                String messageAlerte = String.format(
                        "Alerte de stock sur l'article '%s' (Réf: %s) suite à une consommation sur un ticket. Quantité restante en rayon : %d unités.",
                        article.getDesignation(), article.getReference(), nouvelleQuantite
                );

                // AlerteService va analyser si c'est CRITIQUE (si nouvelleQuantite == 0) ou standard
                alerteService.creerAlerte(article, messageAlerte);
            }
        }
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
                .articleCategorie(stock.getArticle() != null ? stock.getArticle().getCategorie() : null)
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
