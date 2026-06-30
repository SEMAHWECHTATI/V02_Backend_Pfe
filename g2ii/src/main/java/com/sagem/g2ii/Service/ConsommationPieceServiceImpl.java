package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import com.sagem.g2ii.Entity.Inventaire.Stock;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.ConsommationPieceRepository;
import com.sagem.g2ii.Repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConsommationPieceServiceImpl implements IConsommationPieceService {

    private final ConsommationPieceRepository consommationRepository;
    private final ArticleRepository articleRepository;
    private final AlerteService alerteService;
    private final StockRepository stockRepository;
    private final JournalAuditService journalAuditService; // 🌟 Injection du service d'audit

    /**
     * Helper pour extraire l'utilisateur connecté depuis le SecurityContext de Spring
     */
    private Utilisateur getConnectedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Utilisateur) {
                return (Utilisateur) principal;
            }
        } catch (Exception e) {
            // Pas de contexte web / Session expirée
        }
        return null;
    }

    @Override
    @Transactional
    public ConsommationPiece enregistrerConsommation(ConsommationPiece consommation) {
        if (consommation.getArticle() == null || consommation.getArticle().getId() == null) {
            throw new IllegalArgumentException("L'identifiant de l'article est obligatoire pour enregistrer une consommation.");
        }

        Long articleId = consommation.getArticle().getId();
        log.info("🔴 Enregistrement d'une consommation pour l'article ID: {}", articleId);

        // 1. Récupérer la ligne de stock liée à l'article concerné
        Stock stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Aucune ligne de stock trouvée pour l'article ID : " + articleId));

        Article article = stock.getArticle();
        if (article == null) {
            throw new EntityNotFoundException("L'entité Article est absente de la ligne de stock.");
        }

        // 2. Vérifier si la quantité en stock est suffisante
        int quantiteDemandee = consommation.getQuantite();
        int ancienStock = stock.getQuantiteEnStock();

        if (ancienStock < quantiteDemandee) {
            log.error("❌ Stock insuffisant pour la référence : {}. Disponible dans le Stock: {}, Demandé: {}",
                    article.getReference(), ancienStock, quantiteDemandee);
            throw new IllegalArgumentException("Stock insuffisant pour la référence : " + article.getReference()
                    + " (Demandé: " + quantiteDemandee + ", Disponible dans le Stock: " + ancienStock + ")");
        }

        // 3. Déduire la quantité directement de l'entité STOCK
        int nouveauStock = ancienStock - quantiteDemandee;
        stock.setQuantiteEnStock(nouveauStock);

        stockRepository.save(stock);
        log.info("📦 Table Stock mise à jour pour '{}' : {} ➔ {}", article.getDesignation(), ancienStock, nouveauStock);

        // Synchronisation optionnelle de la table Article
        try {
            article.setQuantiteEnStock(nouveauStock);
            articleRepository.save(article);
        } catch (Exception e) {
            log.warn("⚠️ Note: Impossible de synchroniser la quantité directement sur la table Article: {}", e.getMessage());
        }

        // 4. Associer l'article complet et sauvegarder la consommation locale
        consommation.setArticle(article);
        if (consommation.getDateConsommation() == null) {
            consommation.setDateConsommation(LocalDateTime.now());
        }
        ConsommationPiece sauvegarde = consommationRepository.save(consommation);

        // 🌟 LOG D'AUDIT : Consommation de la pièce enregistrée dans le module STOCK
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.APPROBATION_DEMANDE, // Enregistrement/Validation d'une sortie de stock
                "ConsommationPiece",
                sauvegarde.getId(),
                String.format("Sortie de stock de %d unité(s) pour la pièce '%s' (Réf: %s) liée au Ticket Réf: %s.",
                        quantiteDemandee, article.getDesignation(), article.getReference(),
                        consommation.getReferenceTicket() != null ? consommation.getReferenceTicket() : "N/A"),
                String.format("{quantiteStock: %d}", ancienStock),
                String.format("{quantiteStock: %d, consommationId: %d}", nouveauStock, sauvegarde.getId()),
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        // 🚨 ALERTES AUTOMATIQUES
        try {
            int seuilMinimum = stock.getQuantiteMinimum() > 0 ? stock.getQuantiteMinimum() : article.getSeuilMinimum();

            if (nouveauStock <= seuilMinimum) {
                String messageAlerte = String.format(
                        "Alerte de stock sur l'article '%s' (Réf: %s) suite à une consommation sur le ticket '%s'. Quantité restante : %d unités.",
                        article.getDesignation(),
                        article.getReference(),
                        consommation.getReferenceTicket() != null ? consommation.getReferenceTicket() : "N/A",
                        nouveauStock
                );

                alerteService.creerAlerte(article, messageAlerte);
                log.info("📢 Alerte de stock bas émise pour l'article '{}' (Seuil: {})", article.getDesignation(), seuilMinimum);
            }
        } catch (Exception e) {
            log.error("⚠️ Impossible d'émettre l'alerte de stock après consommation: {}", e.getMessage());
        }

        return sauvegarde;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsommationPiece> obtenirConsommationsParTicket(String referenceTicket) {
        return consommationRepository.findByReferenceTicket(referenceTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsommationPiece> obtenirToutesLesConsommations() {
        return consommationRepository.findAll();
    }

    @Override
    @Transactional // 🌟 Ajout du Transactional ici pour sécuriser la modification et la suppression
    public void annulerConsommation(Long id) {
        log.info("🔄 Annulation de la consommation ID: {}", id);

        ConsommationPiece consommation = consommationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consommation non trouvée"));

        Article article = consommation.getArticle();
        int ancienStock = 0;
        int nouveauStock = 0;

        if (article != null) {
            // Récupération de la ligne de stock réelle pour l'annulation
            Stock stock = stockRepository.findByArticleId(article.getId()).orElse(null);

            // On se base en priorité sur la table Stock, sinon sur le fallback de la table Article
            ancienStock = (stock != null) ? stock.getQuantiteEnStock() : article.getQuantiteEnStock();
            nouveauStock = ancienStock + consommation.getQuantite();

            if (stock != null) {
                stock.setQuantiteEnStock(nouveauStock);
                stockRepository.save(stock);
            }

            article.setQuantiteEnStock(nouveauStock);
            articleRepository.save(article);
            log.info("🔄 Stock réincrémenté pour l'article '{}' : {} ➔ {}", article.getDesignation(), ancienStock, nouveauStock);

            // 🚨 ALERTES AUTOMATIQUES : Notification de retour au stock disponible
            try {
                String messageRetour = String.format(
                        "Réapprovisionnement par annulation : La pièce '%s' (Réf: %s) a été réintégrée au stock. Nouveau stock disponible : %d unités.",
                        article.getDesignation(),
                        article.getReference(),
                        nouveauStock
                );
                alerteService.creerAlerte(article, messageRetour);
            } catch (Exception e) {
                log.error("⚠️ Impossible d'émettre la notification de réintégration de stock: {}", e.getMessage());
            }
        }

        // 🌟 LOG D'AUDIT : Tracer la suppression/annulation de la consommation
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.BLOCAGE, // Utilisation de BLOCAGE/ANNULATION pour marquer le rollback
                "ConsommationPiece",
                id,
                String.format("Annulation de la consommation ID %d. Réintégration de %d unité(s) pour la pièce '%s'.",
                        id, consommation.getQuantite(), (article != null ? article.getDesignation() : "Inconnue")),
                String.format("{quantiteStock: %d}", ancienStock),
                String.format("{quantiteStock: %d, action: 'CONSOMMATION_ANNULEE'}", nouveauStock),
                NiveauAudit.WARNING,
                true,
                getConnectedUser()
        );

        consommationRepository.deleteById(id);
        log.info("✅ Consommation ID: {} supprimée de l'historique.", id);
    }
}