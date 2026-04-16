package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.NoteTicketDTO;
import com.sagem.g2ii.Entity.Intervention.NoteTicket;
import com.sagem.g2ii.Service.NoteTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin("*")
public class NoteTicketController {

    @Autowired
    private NoteTicketService noteService;

    /**
     * 📝 Ajouter une note
     */
    @PostMapping("/ajouter")
    public ResponseEntity<?> ajouterNote(@RequestBody NoteTicketDTO dto) {
        try {
            System.out.println("📝 Ajout d'une note");

            if (dto.getContenu() == null || dto.getContenu().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Contenu requis"));
            }

            NoteTicket note = noteService.ajouterNote(dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(note);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📝 Lister les notes d'un ticket
     */
    @GetMapping("/ticket/{idTicket}")
    public ResponseEntity<?> listerNotes(@PathVariable Long idTicket) {
        try {
            List<NoteTicket> notes = noteService.listerNotesTicket(idTicket);
            return ResponseEntity.ok(Map.of(
                    "total", notes.size(),
                    "notes", notes
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🗑️ Supprimer une note
     */
    @DeleteMapping("/{idNote}")
    public ResponseEntity<?> supprimerNote(@PathVariable Long idNote) {
        try {
            noteService.supprimerNote(idNote);
            return ResponseEntity.ok(Map.of("message", "Note supprimée"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}