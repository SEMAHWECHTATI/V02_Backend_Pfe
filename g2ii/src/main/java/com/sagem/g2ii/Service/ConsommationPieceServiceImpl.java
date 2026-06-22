package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import com.sagem.g2ii.Entity.Inventaire.Stock;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.ConsommationPieceRepository;
import com.sagem.g2ii.Repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AlerteService alerteService; // 🛠️ Injection du service d'alerte (Tâche 5)
    private final StockRepository stockRepository;

    @Override
    @Transactional // 🌟 TRÈS IMPORTANT : Annule tout en base de données si une étape plante
    public ConsommationPiece enregistrerConsommation(ConsommationPiece consommation) {
        if (consommation.getArticle() == null || consommation.getArticle().getId() == null) {
            throw new IllegalArgumentException("L'identifiant de l'article est obligatoire pour enregistrer une consommation.");
        }

        Long articleId = consommation.getArticle().getId();
        log.info("🔴 Enregistrement d'une consommation pour l'article ID: {}", articleId);

        // 1. Récupérer la ligne de stock liée à l'article concerné
        Stock stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Aucune ligne de stock trouvée pour l'article ID : " + articleId));

        // Récupération de l'entité Article rattachée au stock pour les alertes et la sauvegarde
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



        stockRepository.save(stock); // Sauvegarde de la table stock
        log.info("📦 Table Stock mise à jour pour '{}' : {} ➔ {}", article.getDesignation(), ancienStock, nouveauStock);

        // Synchronisation de la quantité de l'article si votre colonne existe encore sur la table article (optionnel mais recommandé)
        try {
            article.setQuantiteEnStock(nouveauStock);
            articleRepository.save(article);
        } catch (Exception e) {
            log.warn("⚠️ Note: Impossible de synchroniser la quantité directement sur la table Article (Peut être normal si géré uniquement par le Stock): {}", e.getMessage());
        }

        // 4. Associer l'article complet et sauvegarder la consommation locale
        consommation.setArticle(article);
        if (consommation.getDateConsommation() == null) {
            consommation.setDateConsommation(LocalDateTime.now());
        }
        ConsommationPiece sauvegarde = consommationRepository.save(consommation);

        // 🚨 ALERTES AUTOMATIQUES : Détection basée sur les seuils configurés dans le stock ou l'article
        try {
            // Utilisation du seuil minimum défini (porté par le stock ou par l'article)
            int seuilMinimum = stock.getQuantiteMinimum() > 0 ? stock.getQuantiteMinimum() : article.getSeuilMinimum();

            if (nouveauStock <= seuilMinimum) {
                String messageAlerte = String.format(
                        "Alerte de stock sur l'article '%s' (Réf: %s) suite à une consommation sur le ticket '%s'. Quantité restante : %d unités.",
                        article.getDesignation(),
                        article.getReference(),
                        consommation.getReferenceTicket() != null ? consommation.getReferenceTicket() : "N/A",
                        nouveauStock
                );

                // Envoi de l'alerte temps réel
                alerteService.creerAlerte(article, messageAlerte);
                log.info("📢 Alerte de stock bas émise pour l'article '{}' (Seuil: {})", article.getDesignation(), seuilMinimum);
            }
        } catch (Exception e) {
            // Bloc sécurisé : une erreur d'envoi d'alerte ne doit jamais annuler la transaction principale
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
    public void annulerConsommation(Long id) {
        log.info("🔄 Annulation de la consommation ID: {}", id);

        ConsommationPiece consommation = consommationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consommation non trouvée"));

        Article article = consommation.getArticle();
        if (article != null) {
            int ancienStock = article.getQuantiteEnStock();
            int nouveauStock = ancienStock + consommation.getQuantite();

            article.setQuantiteEnStock(nouveauStock);
            articleRepository.save(article);
            log.info("🔄 Stock réincrémenté pour l'article '{}' : {} ➔ {}", article.getDesignation(), ancienStock, nouveauStock);

            // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Notification de retour au stock disponible
            try {
                String messageRetour = String.format(
                        "Réapprovisionnement par annulation : La pièce '%s' (Réf: %s) a été réintégrée au stock. Nouveau stock disponible : %d unités.",
                        article.getDesignation(),
                        article.getReference(),
                        nouveauStock
                );

                // On notifie en temps réel sur l'interface Angular que le matériel est de nouveau disponible
                alerteService.creerAlerte(article, messageRetour);
            } catch (Exception e) {
                log.error("⚠️ Impossible d'émettre la notification de réintégration de stock: {}", e.getMessage());
            }
        }

        consommationRepository.deleteById(id);
        log.info("✅ Consommation ID: {} supprimée de l'historique.", id);
    }
}