package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Intervention.HistoriqueTicket;
import com.sagem.g2ii.Service.HistoriqueTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/historique")
@CrossOrigin("*")
public class HistoriqueTicketController {

    @Autowired
    private HistoriqueTicketService historiqueService;

    /**
     * 📜 Récupérer l'historique d'un ticket
     */
    @GetMapping("/ticket/{idTicket}")
    public ResponseEntity<?> getHistorique(@PathVariable Long idTicket) {
        try {
            List<HistoriqueTicket> historique = historiqueService.getHistoriqueTicket(idTicket);
            return ResponseEntity.ok(Map.of(
                    "total", historique.size(),
                    "historique", historique
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}