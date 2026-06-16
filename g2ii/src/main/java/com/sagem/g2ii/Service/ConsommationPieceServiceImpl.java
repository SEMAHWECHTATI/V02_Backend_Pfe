package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.ConsommationPieceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConsommationPieceServiceImpl implements IConsommationPieceService {

    private final ConsommationPieceRepository consommationRepository;
    private final ArticleRepository articleRepository;
    private final AlerteService alerteService; // 🛠️ Injection du service d'alerte (Tâche 5)

    @Override
    public ConsommationPiece enregistrerConsommation(ConsommationPiece consommation) {
        log.info("🔴 Enregistrement d'une consommation manuelle/ticket pour l'article ID: {}", consommation.getArticle().getId());

        // 1. Récupérer l'article concerné
        Article article = articleRepository.findById(consommation.getArticle().getId())
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'ID : " + consommation.getArticle().getId()));

        // 2. Vérifier si le stock est suffisant
        if (article.getQuantiteEnStock() < consommation.getQuantite()) {
            log.error("❌ Stock insuffisant pour la référence : {}. Disponible: {}, Demandé: {}",
                    article.getReference(), article.getQuantiteEnStock(), consommation.getQuantite());
            throw new IllegalArgumentException("Stock insuffisant pour la référence : " + article.getReference()
                    + " (Demandé: " + consommation.getQuantite() + ", Disponible: " + article.getQuantiteEnStock() + ")");
        }

        // 3. Déduire la quantité du stock de l'article
        int ancienStock = article.getQuantiteEnStock();
        int nouveauStock = ancienStock - consommation.getQuantite();
        article.setQuantiteEnStock(nouveauStock);
        articleRepository.save(article);

        log.info("📦 Stock de l'article '{}' mis à jour : {} ➔ {}", article.getDesignation(), ancienStock, nouveauStock);

        // 4. Associer l'article complet et sauvegarder la consommation
        consommation.setArticle(article);
        ConsommationPiece sauvegarde = consommationRepository.save(consommation);

        // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Détection de seuil bas ou rupture immédiate
        try {
            if (nouveauStock <= article.getSeuilMinimum()) {
                String messageAlerte = String.format(
                        "Alerte de stock sur l'article '%s' (Réf: %s) suite à une consommation sur le ticket '%s'. Quantité restante : %d unités.",
                        article.getDesignation(),
                        article.getReference(),
                        consommation.getReferenceTicket() != null ? consommation.getReferenceTicket() : "N/A",
                        nouveauStock
                );

                // AlerteService se chargera d'analyser la sévérité (CRITIQUE si nouveauStock == 0)
                // et d'envoyer l'alerte sur Angular (WebSockets) et par Mail aux personnes concernées.
                alerteService.creerAlerte(article, messageAlerte);
            }
        } catch (Exception e) {
            // Bloc sécurisé : une erreur d'envoi d'alerte ne doit jamais annuler l'enregistrement de la pièce
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