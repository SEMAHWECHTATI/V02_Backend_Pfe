package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsommationPieceRepository extends JpaRepository<ConsommationPiece, Long> {
    // Récupérer toutes les pièces consommées pour un ticket spécifique
    List<ConsommationPiece> findByReferenceTicket(String referenceTicket);

    // Récupérer l'historique des consommations pour un article donné
    List<ConsommationPiece> findByArticleId(Long articleId);
}