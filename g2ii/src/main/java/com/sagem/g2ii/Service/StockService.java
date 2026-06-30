package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.StockDTO;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Stock;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JournalAuditService journalAuditService; // 🌟 Injection du service d'audit

    /**
     * Helper pour extraire l'utilisateur connecté depuis le SecurityContext de Spring Security
     */
    private Utilisateur getConnectedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Utilisateur) {
                return (Utilisateur) principal;
            }
        } catch (Exception e) {
            // Pas de contexte de session actif (ex: script système ou traitement asynchrone)
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<StockDTO> recupererToutLeStock() {
        return stockRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Créer un stock pour un article ET diminuer sa quantité dans le catalogue
     */
    @Transactional
    public StockDTO creerStock(Long articleId, StockDTO dto) {
        log.info("📊 Création stock pour article: {}", articleId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        int quantiteATransferer = dto.getQuantiteEnStock();

        if (article.getQuantiteEnStock() < quantiteATransferer) {
            throw new IllegalArgumentException("Quantité insuffisante dans le catalogue d'articles. Disponible : "
                    + article.getQuantiteEnStock() + ", Demandé : " + quantiteATransferer);
        }

        int nouvelleQuantiteCatalogue = article.getQuantiteEnStock() - quantiteATransferer;
        article.setQuantiteEnStock(nouvelleQuantiteCatalogue);
        articleRepository.save(article);
        log.info("📉 Article ID {} mis à jour dans le catalogue. Ancienne qté -> Nouvelle qté : {}", articleId, nouvelleQuantiteCatalogue);

        Stock stock = Stock.builder()
                .article(article)
                .codeBarresArticle(dto.getCodeBarresArticle())
                .quantiteEnStock(quantiteATransferer)
                .quantiteCritique(dto.getQuantiteCritique())
                .quantiteMinimum(dto.getQuantiteMinimum())
                .prixUnitaire(dto.getPrixUnitaire())
                .build();

        Stock saved = stockRepository.save(stock);
        log.info("✅ Stock créé avec ID: {}", saved.getId());

        // 🌟 LOG D'AUDIT : Initialisation d'une nouvelle ligne de Stock physique
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.CREATE_STOCK,
                "Stock",
                saved.getId(),
                String.format("Initialisation de l'emplacement de stock pour la pièce '%s' (Réf: %s) avec un transfert initial de %d unité(s).",
                        article.getDesignation(), article.getReference(), quantiteATransferer),
                null,
                String.format("{articleId: %d, quantiteInitiale: %d}", articleId, quantiteATransferer),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        return convertToDTO(saved);
    }

    @Transactional
    public void mettreAJourLocalisation(Long stockId, String nouvelleLocalisation) {
        log.info("Mise à jour de la localisation du stock ID {}: -> {}", stockId, nouvelleLocalisation);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Impossible de mettre à jour la localisation : Stock non trouvé avec l'ID " + stockId));

        // 🌟 Logique d'audit sur l'ancienne valeur avant sauvegarde (ex: champ emplacement si disponible)
        String ancienneLocalisationStr = stock.getArticle() != null ? stock.getArticle().getDesignation() : "Inconnue";

        stockRepository.save(stock);
        log.info("✅ Localisation du stock ID {} mise à jour avec succès.", stockId);

        // 🌟 LOG D'AUDIT : Changement d'emplacement matériel
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.UPDATE_LOCALISATION,
                "Stock",
                stockId,
                String.format("Mise à jour de la localisation pour l'emplacement de stock ID %d.", stockId),
                String.format("{localisation: '%s'}", ancienneLocalisationStr),
                String.format("{localisation: '%s'}", nouvelleLocalisation),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );
    }

    @Transactional
    public void supprimerStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Impossible de supprimer : aucun stock trouvé avec l'ID " + stockId));

        String detailsStock = String.format("{id: %d, quantiteRestante: %d}", stock.getId(), stock.getQuantiteEnStock());

        stockRepository.delete(stock);
        log.info("✅ Stock supprimé de la base de données avec l'ID : {}", stockId);

        // 🌟 LOG D'AUDIT : Retrait physique définitif d'un rayon de stock
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.DELETE_MATERIEL,
                "Stock",
                stockId,
                String.format("Suppression définitive de la ligne de stock ID %d.", stockId),
                detailsStock,
                null,
                NiveauAudit.WARNING, // Alerte système sur la destruction d'un historique de rayon
                true,
                getConnectedUser()
        );
    }

    /**
     * ✅ Mettre à jour la quantité
     */
    @Transactional
    public void mettreAJourQuantite(Long stockId, Integer nouvelleQuantite) {
        log.info("🔄 Mise à jour quantité stock: {}", stockId);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        Integer ancienneQuantite = stock.getQuantiteEnStock();
        stock.setQuantiteEnStock(nouvelleQuantite);
        stockRepository.save(stock);

        Article article = stock.getArticle();
        if (article != null) {
            article.setQuantiteEnStock(nouvelleQuantite);
            articleRepository.save(article);
        }

        log.info("✅ Quantité mise à jour: {} -> {}", ancienneQuantite, nouvelleQuantite);

        // 🌟 LOG D'AUDIT : Ajustement/Inventaire manuel des volumes en stock
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.UPDATE_MATERIEL,
                "Stock",
                stockId,
                String.format("Ajustement manuel de l'inventaire pour l'article '%s' : %d ➔ %d.",
                        (article != null ? article.getDesignation() : "Inconnu"), ancienneQuantite, nouvelleQuantite),
                String.format("{quantite: %d}", ancienneQuantite),
                String.format("{quantite: %d}", nouvelleQuantite),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        if (nouvelleQuantite <= stock.getQuantiteCritique() && article != null) {
            alerteService.creerAlerte(article, "Stock critique!");
        }
    }

    /**
     * ✅ Récupérer stock faible
     */
    @Transactional(readOnly = true)
    public List<StockDTO> getStockFaible() {
        return stockRepository.findStockFaible().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔍 Récupérer le stock associé à un article
     */
    @Transactional(readOnly = true)
    public StockDTO recupererStockParArticle(Long articleId) {
        log.info("🔍 Recherche du stock pour l'article ID: {}", articleId);
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

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        if (stock.getQuantiteEnStock() == null) {
            stock.setQuantiteEnStock(0);
        }

        int ancienneQuantite = stock.getQuantiteEnStock();
        int nouvelleQuantite = ancienneQuantite - quantiteADeduire;
        if (nouvelleQuantite < 0) {
            throw new RuntimeException("Erreur : Stock insuffisant en magasin ! Quantité disponible : " + stock.getQuantiteEnStock());
        }

        stock.setQuantiteEnStock(nouvelleQuantite);
        stockRepository.save(stock);
        log.info("✅ Table STOCK mise à jour. Nouvelle quantité physique: {}", nouvelleQuantite);

        Article article = stock.getArticle();

        // 🌟 LOG D'AUDIT : Traçabilité des consommations directes sur le stock
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.UPDATE_MATERIEL,
                "Stock",
                stockId,
                String.format("Décrémentation de stock de %d unité(s) pour l'article '%s' suite à une intervention.",
                        quantiteADeduire, (article != null ? article.getDesignation() : "Inconnu")),
                String.format("{quantite: %d}", ancienneQuantite),
                String.format("{quantite: %d}", nouvelleQuantite),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        if (article != null) {
            if (nouvelleQuantite <= article.getSeuilMinimum()) {
                String messageAlerte = String.format(
                        "Alerte de stock sur l'article '%s' (Réf: %s) suite à une consommation sur un ticket. Quantité restante en rayon : %d unités.",
                        article.getDesignation(), article.getReference(), nouvelleQuantite
                );
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
                .prixUnitaire(stock.getPrixUnitaire())
                .articleId(stock.getArticle() != null ? stock.getArticle().getId() : null)
                .articleCategorie(stock.getArticle() != null ? stock.getArticle().getCategorie() : null)
                .articleReference(stock.getArticle() != null ? stock.getArticle().getReference() : null)
                .articleDesignation(stock.getArticle() != null ? stock.getArticle().getDesignation() : null)
                .articleTypeArticle(stock.getArticle() != null ? stock.getArticle().getTypeArticle().getLabel() : null)
                .articleStatut(stock.getArticle() != null ? stock.getArticle().getStatut().getLabel() : null)
                .valeurTotale(valeurTotale)
                .estFaible(stock.getQuantiteEnStock() != null && stock.getQuantiteMinimum() != null &&
                        stock.getQuantiteEnStock() <= stock.getQuantiteMinimum())
                .estCritique(stock.getQuantiteEnStock() != null && stock.getQuantiteCritique() != null &&
                        stock.getQuantiteEnStock() <= stock.getQuantiteCritique())
                .build();
    }
}