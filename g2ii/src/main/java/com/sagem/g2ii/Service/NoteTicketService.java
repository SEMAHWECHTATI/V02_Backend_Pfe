package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.NoteTicketDTO;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.TypeNote;
import com.sagem.g2ii.Entity.Intervention.NoteTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Repository.NoteTicketRepo;
import com.sagem.g2ii.Repository.TicketRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoteTicketService {

    @Autowired
    private NoteTicketRepo noteRepository;

    @Autowired
    private TicketRepo ticketRepository;

    @Autowired
    private IntUtilisateur utilisateurRepository;

    /**
     * 📝 Ajouter une note
     */
    @Transactional
    public NoteTicket ajouterNote(NoteTicketDTO dto) {
        System.out.println("📝 Ajout d'une note au ticket ID: " + dto.getIdTicket());

        Ticket ticket = ticketRepository.findById(dto.getIdTicket())
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        Utilisateur utilisateur = utilisateurRepository.findById(dto.getIdUtilisateur())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        TypeNote type = dto.getType() != null ?
                TypeNote.valueOf(dto.getType()) : TypeNote.COMMENTAIRE;

        NoteTicket note = NoteTicket.builder()
                .contenu(dto.getContenu())
                .type(type)
                .ticket(ticket)
                .utilisateur(utilisateur)
                .date(LocalDate.now())
                .build();

        NoteTicket noteSauvegardee = noteRepository.save(note);

        System.out.println("✅ Note ajoutée");

        return noteSauvegardee;
    }

    /**
     * 📝 Lister les notes d'un ticket
     */
    public List<NoteTicket> listerNotesTicket(Long idTicket) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        return noteRepository.findByTicket(ticket);
    }

    /**
     * 🗑️ Supprimer une note
     */
    @Transactional
    public void supprimerNote(Long idNote) {
        noteRepository.deleteById(idNote);
        System.out.println("✅ Note supprimée");
    }
}