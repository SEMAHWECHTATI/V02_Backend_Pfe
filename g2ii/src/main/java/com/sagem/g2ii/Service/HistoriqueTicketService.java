package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Intervention.HistoriqueTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.HistoriqueTicketRepo;
import com.sagem.g2ii.Repository.TicketRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HistoriqueTicketService {

    @Autowired
    private HistoriqueTicketRepo historiqueRepository;

    @Autowired
    private TicketRepo ticketRepository;

    /**
     * 📝 Tracer une modification
     */
    @Transactional
    public HistoriqueTicket tracer(Ticket ticket, Utilisateur acteur,
                                   String champModifie, String ancienneValeur,
                                   String nouvelleValeur) {
        System.out.println("📝 Traçage: " + champModifie);

        HistoriqueTicket historique = HistoriqueTicket.builder()
                .ticket(ticket)
                .utilisateur(acteur)
                .champModifie(champModifie)
                .ancienneValeur(ancienneValeur)
                .nouvelleValeur(nouvelleValeur)
                .date(LocalDate.now())
                .build();

        return historiqueRepository.save(historique);
    }

    /**
     * 📜 Récupérer l'historique d'un ticket
     */
    public List<HistoriqueTicket> getHistoriqueTicket(Long idTicket) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        return historiqueRepository.findByTicket(ticket);
    }
}