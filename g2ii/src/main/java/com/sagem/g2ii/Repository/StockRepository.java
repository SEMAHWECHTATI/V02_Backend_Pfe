package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Inventaire.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByArticleId(Long articleId);
    Optional<Stock> findByCodeBarresArticle(String codeBarres);

    @Query("SELECT s FROM Stock s WHERE s.quantiteEnStock <= s.quantiteMinimum")
    List<Stock> findStockFaible();

    // 💰 Calcul financier : Somme de (quantiteEnStock * prixUnitaire)
    // Casté en double ou renvoyé en BigDecimal pour l'API
    @Query("SELECT SUM(s.quantiteEnStock * s.prixUnitaire) FROM Stock s")
    BigDecimal getValeurGlobaleInventaire();

    // ⚠️ Alerte Stock Faible : Quantité est tombée sous le minimum, mais reste au-dessus du seuil critique
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantiteEnStock <= s.quantiteMinimum AND s.quantiteEnStock > s.quantiteCritique")
    Long countStocksFaibles();

    // 🚨 Alerte Rupture : Quantité est inférieure ou égale au seuil critique absolu
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantiteEnStock <= s.quantiteCritique")
    Long countRupturesCritiques();

    @Query(value = "SELECT valeur_globale_parc, total_stocks_faibles, total_ruptures FROM vue_kpi_finance_alertes", nativeQuery = true)
    Map<String, Object> getFinanceDashboardStats();
}