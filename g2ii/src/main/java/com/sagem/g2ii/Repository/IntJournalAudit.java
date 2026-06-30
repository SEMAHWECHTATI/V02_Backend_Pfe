package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Authentification.JournalAudit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IntJournalAudit extends JpaRepository<JournalAudit, Long> {

    // 🔥 LA CORRECTION : Force Hibernate à joindre l'utilisateur ET ses groupes en une seule requête SQL
    @EntityGraph(attributePaths = {"utilisateur", "utilisateur.groupes"})
    List<JournalAudit> findByDateActionBefore(LocalDateTime date);

    // Supprime les logs antérieurs à la date donnée (Nécessite @Transactional)
    @Transactional
    void deleteByDateActionBefore(LocalDateTime date);

}
