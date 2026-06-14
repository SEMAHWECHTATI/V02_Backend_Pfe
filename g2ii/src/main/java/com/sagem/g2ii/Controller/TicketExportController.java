package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Service.TicketService;
import com.sagem.g2ii.Service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketExportController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ExportService exportService;

    /**
     * 📄 Exporter un ticket en PDF
     */
    @GetMapping("/{idTicket}/export/pdf")
    public ResponseEntity<byte[]> exportTicketPDF(@PathVariable Long idTicket) {
        try {
            // Récupérer le ticket
            Ticket ticket = ticketService.getTicketById(idTicket);

            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            // Générer le PDF
            byte[] pdfContent = exportService.generateTicketPDF(ticket);

            // Créer les headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    "Ticket_" + ticket.getReference() + ".pdf"
            );

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("❌ Erreur export PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 📊 Exporter un ticket en Excel
     */
    @GetMapping("/{idTicket}/export/excel")
    public ResponseEntity<byte[]> exportTicketExcel(@PathVariable Long idTicket) {
        try {
            // Récupérer le ticket
            Ticket ticket = ticketService.getTicketById(idTicket);

            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            // Générer l'Excel
            byte[] excelContent = exportService.generateTicketExcel(ticket);

            // Créer les headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
            );
            headers.setContentDispositionFormData(
                    "attachment",
                    "Ticket_" + ticket.getReference() + ".xlsx"
            );

            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("❌ Erreur export Excel: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}