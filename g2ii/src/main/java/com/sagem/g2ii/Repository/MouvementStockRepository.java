package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.TypeMouvement;
import com.sagem.g2ii.Entity.Inventaire.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
    List<MouvementStock> findByStockId(Long stockId);
    List<MouvementStock> findByType(TypeMouvement type);

    @Query("SELECT m FROM MouvementStock m WHERE m.dateMouvement BETWEEN :debut AND :fin")
    List<MouvementStock> findMouvementsBetweenDates(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );

    List<MouvementStock> findByReferenceTicket(String referenceTicket);
}