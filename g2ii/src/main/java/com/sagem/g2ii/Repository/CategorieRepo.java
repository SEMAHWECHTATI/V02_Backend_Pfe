package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.TypeTicket;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategorieRepo extends JpaRepository<Categorie, Long> {
    List<Categorie> findByActifTrue();

    // 🎯 Remplacer l'Optional par une List pour encaisser les résultats multiples sans crasher
    List<Categorie> findByType(TypeTicket type);
}
