package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Authentification.DemandeInscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntdemandeInscri extends JpaRepository<DemandeInscription, Long> {

   DemandeInscription findByStatut (String statut);

}
