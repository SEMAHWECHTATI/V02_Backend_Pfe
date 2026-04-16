package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.TypeNote;
import com.sagem.g2ii.Entity.Intervention.NoteTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteTicketRepo extends JpaRepository<NoteTicket, Long> {

    List<NoteTicket> findByTicket(Ticket ticket);

    List<NoteTicket> findByTicketAndType(Ticket ticket, TypeNote type);
}