package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Intervention.HistoriqueTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueTicketRepo extends JpaRepository<HistoriqueTicket, Long> {

    List<HistoriqueTicket> findByTicket(Ticket ticket);
}