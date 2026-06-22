package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Entity.Intervention.SLA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SLARepository extends JpaRepository<SLA, Long> {

    // 🔍 Ajoutez "Optional<...>" ici :
    Optional<SLA> findByCategorieAndPriorite(Categorie categorie, Priorite priorite);

    List<SLA> findByCategorieIdCategorie(Long idCategorie);

    Optional<SLA> findByCategorieIdCategorieAndPriorite(Long idCategorie, Priorite priorite);
}