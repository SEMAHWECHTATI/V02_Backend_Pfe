package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Inventaire.ArticleLocalisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleLocalisationRepository extends JpaRepository<ArticleLocalisation, String> {

    List<ArticleLocalisation> findByArticleId(Long articleId);

    List<ArticleLocalisation> findByLocalisationId(Long localisationId);
}
