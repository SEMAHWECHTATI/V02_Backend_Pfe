package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.TicketCreationDTO;
import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin("*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    /**
     * 1️⃣ Créer un ticket
     */
    @PostMapping("/creer/{idCategorie}")
    public ResponseEntity<?> creerTicket(
            @RequestBody TicketCreationDTO dto,
            @PathVariable Long idCategorie) {
        try {
            System.out.println("📨 Création de ticket");

            if (dto.getTitre() == null || dto.getTitre().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Titre requis"));
            }

            if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Description requise"));
            }

            dto.setCategorieId(idCategorie);

            Ticket ticket = ticketService.creerTicket(dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);

        } catch (RuntimeException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 2️⃣ Lister tous les tickets
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllTickets() {
        try {
            List<Ticket> tickets = ticketService.listerTous();
            return ResponseEntity.ok(Map.of(
                    "total", tickets.size(),
                    "tickets", tickets
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 3️⃣ Récupérer un ticket par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable Long id) {
        try {
            Ticket ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 4️⃣ Lister les tickets du demandeur
     */
    @GetMapping("/demandeur/{idDemandeur}")
    public ResponseEntity<?> getTicketsByDemandeur(@PathVariable Long idDemandeur) {
        try {
            List<Ticket> tickets = ticketService.listerParDemandeur(idDemandeur);
            return ResponseEntity.ok(Map.of(
                    "total", tickets.size(),
                    "tickets", tickets
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 5️⃣ Lister les tickets du groupe
     */
    @GetMapping("/groupe/{idGroupe}")
    public ResponseEntity<?> getTicketsByGroupe(@PathVariable Long idGroupe) {
        try {
            List<Ticket> tickets = ticketService.listerParGroupe(idGroupe);
            return ResponseEntity.ok(Map.of(
                    "total", tickets.size(),
                    "tickets", tickets
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 6️⃣ Démarrer un ticket
     */
    @PutMapping("/{idTicket}/demarrer")
    public ResponseEntity<?> demarrerTicket(
            @PathVariable Long idTicket,
            @RequestParam Long idUtilisateur) {
        try {
            Ticket ticket = ticketService.demarrerTicket(idTicket, idUtilisateur);
            return ResponseEntity.ok(Map.of(
                    "message", "Ticket démarré",
                    "ticket", ticket
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 7️⃣ Résoudre un ticket
     */
    @PutMapping("/{idTicket}/resoudre")
    public ResponseEntity<?> resoudreTicket(
            @PathVariable Long idTicket,
            @RequestParam Long idUtilisateur,
            @RequestParam String noteResolution) {
        try {
            Ticket ticket = ticketService.resoudreTicket(idTicket, idUtilisateur, noteResolution);
            return ResponseEntity.ok(Map.of(
                    "message", "Ticket résolu",
                    "slaRespecte", ticket.getSlaRespecte(),
                    "ticket", ticket
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 8️⃣ Clôturer un ticket
     */
    @PutMapping("/{idTicket}/cloturer")
    public ResponseEntity<?> cloturerTicket(
            @PathVariable Long idTicket,
            @RequestParam Long idUtilisateur,
            @RequestParam String roleUtilisateur) {
        try {
            Ticket ticket = ticketService.cloturerTicket(idTicket, idUtilisateur, roleUtilisateur);
            return ResponseEntity.ok(Map.of(
                    "message", "Ticket clôturé",
                    "ticket", ticket
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 9️⃣ Supprimer un ticket
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            ticketService.supprimerTicket(id);
            return ResponseEntity.ok(Map.of("message", "Ticket supprimé"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}