package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
import com.sagem.g2ii.Entity.Inventaire.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByReference(String reference);
    Optional<Article> findByCodeBarres(String codeBarres);
    List<Article> findByTypeArticle(TypeArticle type);
    List<Article> findByStatut(StatutArticle statut);
    List<Article> findByLocalisationId(Long localisationId);

    // ✅ Articles avec stock faible
    @Query("SELECT a FROM Article a WHERE a.quantiteEnStock <= a.seuilMinimum")
    List<Article> findArticlesAvecStockFaible();

    // ✅ Articles avec stock critique
    @Query("SELECT a FROM Article a WHERE a.quantiteEnStock <= a.seuilCritique")
    List<Article> findArticlesAvecStockCritique();

    // ✅ Valeur totale du stock
    @Query("SELECT COALESCE(SUM(a.quantiteEnStock * a.prixUnitaire), 0) FROM Article a WHERE a.statut != 'ARCHIVÉ'")
    BigDecimal getTotalInventoryValue();

    // ✅ Valeur par type d'article
    @Query("SELECT SUM(a.quantiteEnStock * a.prixUnitaire) FROM Article a WHERE a.typeArticle = :type")
    BigDecimal getTotalValueByType(@Param("type") TypeArticle type);

    // ✅ Valeur par statut
    @Query("SELECT SUM(a.quantiteEnStock * a.prixUnitaire) FROM Article a WHERE a.statut = :statut")
    BigDecimal getTotalValueByStatut(@Param("statut") StatutArticle statut);

    // ✅ Nombre total d'articles en stock
    @Query("SELECT SUM(a.quantiteEnStock) FROM Article a")
    Integer getTotalQuantityInStock();

    // ✅ Nombre total d'articles uniques
    @Query("SELECT COUNT(a) FROM Article a")
    Long getTotalArticles();

    // ✅ Recherche multicritère
    @Query("SELECT a FROM Article a WHERE LOWER(a.designation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Article> searchByKeyword(@Param("keyword") String keyword);



    @Query("SELECT SUM(a.prixUnitaire * a.quantiteEnStock) FROM Article a")
    BigDecimal calculerValeurTotaleInventaire();

    // 🔄 CORRIGÉ : Utilisation de quantiteEnStock et prixUnitaire
    @Query("SELECT SUM(a.quantiteEnStock * a.prixUnitaire) FROM Article a")
    Double calculateTotalAssetValue();

    // 🔄 CORRIGÉ : Utilisation de quantiteEnStock au lieu de quantite
    @Query("SELECT COUNT(a) FROM Article a WHERE a.quantiteEnStock <= a.seuilCritique")
    long countOutOfStockArticles();

    // ⚠️ Attention : Vérifie bien que "tauxDisponibilite" existe dans ton entité Article.java
    @Query("SELECT AVG(a.tauxDisponibilite) FROM Article a")
    Double getAverageAvailabilityRate();

}