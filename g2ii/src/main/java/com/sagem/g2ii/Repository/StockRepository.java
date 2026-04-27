package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Inventaire.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByArticleId(Long articleId);
    Optional<Stock> findByCodeBarresArticle(String codeBarres);

    @Query("SELECT s FROM Stock s WHERE s.quantiteEnStock <= s.quantiteMinimum")
    List<Stock> findStockFaible();
}