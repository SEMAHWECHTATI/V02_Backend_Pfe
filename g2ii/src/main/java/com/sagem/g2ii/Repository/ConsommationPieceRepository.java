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
    List<ConsommationPiece> findByArticleId(Long articleId);
    List<ConsommationPiece> findByReferenceTicket(String referenceTicket);

    @Query("SELECT c FROM ConsommationPiece c WHERE c.dateConsommation BETWEEN :debut AND :fin")
    List<ConsommationPiece> findConsommationsBetweenDates(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );
}