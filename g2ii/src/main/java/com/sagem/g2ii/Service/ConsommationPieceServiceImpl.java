package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.ConsommationPieceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsommationPieceServiceImpl implements IConsommationPieceService {

    private final ConsommationPieceRepository consommationRepository;
    private final ArticleRepository articleRepository; // ✅ Injection de dépendance ajoutée

    @Override
    public ConsommationPiece enregistrerConsommation(ConsommationPiece consommation) {
        // 1. Récupérer l'article concerné depuis l'instance injectée
        Article article = articleRepository.findById(consommation.getArticle().getId())
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'ID : " + consommation.getArticle().getId()));

        // 2. Vérifier si le stock est suffisant
        if (article.getQuantiteEnStock() < consommation.getQuantite()) {
            throw new IllegalArgumentException("Stock insuffisant pour la référence : " + article.getReference()
                    + " (Demandé: " + consommation.getQuantite() + ", Disponible: " + article.getQuantiteEnStock() + ")");
        }

        // 3. Déduire la quantité du stock de l'article
        article.setQuantiteEnStock(article.getQuantiteEnStock() - consommation.getQuantite());
        articleRepository.save(article);

        // 4. Associer l'article complet et sauvegarder la consommation
        consommation.setArticle(article);
        return consommationRepository.save(consommation);
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
        // 🚀 Bonus Sécurité : Réincrémenter le stock de l'article si la consommation est annulée
        ConsommationPiece consommation = consommationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consommation non trouvée"));

        Article article = consommation.getArticle();
        if (article != null) {
            article.setQuantiteEnStock(article.getQuantiteEnStock() + consommation.getQuantite());
            articleRepository.save(article);
        }

        consommationRepository.deleteById(id);
    }
}