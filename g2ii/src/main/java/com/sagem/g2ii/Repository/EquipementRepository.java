package com.sagem.g2ii.Repository;


import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Equipement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipementRepository extends JpaRepository<Equipement, Long> {
    long countByStatut(StatutArticle statut);




    Optional<Equipement> findByNumeroSerie(String numeroSerie);

    Page<Equipement> findByStatut(StatutArticle statut, Pageable pageable);

    Page<Equipement> findByArticle(Article article, Pageable pageable);

    Page<Equipement> findByDesignationContainingIgnoreCase(String designation, Pageable pageable);

    List<Equipement> findByStatut(StatutArticle statut);

    Page<Equipement> findAll(Pageable pageable);

    @Query("SELECT COUNT(e) FROM Equipement e WHERE e.statut = 'ACTIF'")
    long countActiveEquipments();

    // Rechercher par code-barres (unique)
    Optional<Equipement> findByCodeBarres(String codeBarres);



    // Filtrer les équipements par localisation physique
    List<Equipement> findByLocalisationId(Long localisationId);
}
