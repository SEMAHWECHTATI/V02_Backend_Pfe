package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Authentification.DemandeInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IntdemandeInscri extends JpaRepository<DemandeInscription, Long> {

   DemandeInscription findByStatut (String statut);
    // Récupérer uniquement les demandes NON archivées (actives)
    List<DemandeInscription> findByArchiveeFalse();

    // Récupérer uniquement les demandes archivées
    List<DemandeInscription> findByArchiveeTrue();

}
