package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepo extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findTopByReferenceStartingWithOrderByReferenceDesc(String prefix);

    List<Ticket> findByDemandeur_Id(Long idDemandeur);

    List<Ticket> findByGroupeAssigne_Id(Long idGroupe);

    List<Ticket> findByStatut(StatutTicket statut);

    long countByStatut(StatutTicket statut);
}