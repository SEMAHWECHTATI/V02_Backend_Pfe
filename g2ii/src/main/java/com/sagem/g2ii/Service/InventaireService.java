package com.sagem.g2ii.Service;


import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Equipement;
import com.sagem.g2ii.Entity.Inventaire.Localisation;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.EquipementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventaireService {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private EquipementRepository equipementRepository;

    public void receptionnerMateriel(Long articleId, Integer quantite, List<String> numerosDeSerie, Localisation loc) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article introuvable"));

        if (article.getTypeArticle() == TypeArticle.SERIALISE) {
            // REGLE : Si sérialisé, la quantité reçue doit correspondre exactement au nombre de S/N fournis
            if (numerosDeSerie == null || numerosDeSerie.size() != quantite) {
                throw new IllegalArgumentException("Chaque équipement sérialisé doit avoir son numéro de série unique.");
            }

            // On crée autant de lignes uniques dans la table 'Equipement' que de S/N
            for (String sn : numerosDeSerie) {
                Equipement equipement = Equipement.builder()
                        .article(article)
                        .numeroSerie(sn)
                        .codeBarres("INV-" + sn) // Génération automatique du code-barres unique
                        .statut(StatutArticle.ACTIF)
                        .localisation(loc)
                        .build();
                equipementRepository.save(equipement);
            }
        }

        // Dans tous les cas, on met à jour le compteur global de l'article (ou de la table Stock)
        article.setQuantiteEnStock(article.getQuantiteEnStock() + quantite);
        articleRepository.save(article);
    }
}