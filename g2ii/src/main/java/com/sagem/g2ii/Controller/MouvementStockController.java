package com.sagem.g2ii.Controller;


import com.sagem.g2ii.DTOs.MouvementStockDTO;
import com.sagem.g2ii.Service.MouvementStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/mouvements")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class MouvementStockController {

    private final MouvementStockService mouvementService;

    /**
     * 📥 Enregistrer entrée
     */
    @PostMapping("/entree")
    public ResponseEntity<?> enregistrerEntree(
            @RequestParam Long stockId,
            @RequestParam Integer quantite,
            @RequestParam String justification) {
        try {
            MouvementStockDTO mouvement = mouvementService.enregistrerEntree(stockId, quantite, justification);
            return ResponseEntity.ok(Map.of("message", "✅ Entrée enregistrée", "mouvement", mouvement));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📤 Enregistrer sortie
     */
    @PostMapping("/sortie")
    public ResponseEntity<?> enregistrerSortie(
            @RequestParam Long stockId,
            @RequestParam Integer quantite,
            @RequestParam String justification) {
        try {
            MouvementStockDTO mouvement = mouvementService.enregistrerSortie(stockId, quantite, justification);
            return ResponseEntity.ok(Map.of("message", "✅ Sortie enregistrée", "mouvement", mouvement));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔄 Enregistrer transfert
     */
    @PostMapping("/transfert")
    public ResponseEntity<?> enregistrerTransfert(
            @RequestParam Long stockId,
            @RequestParam Integer quantite,
            @RequestParam String locSource,
            @RequestParam String locDest,
            @RequestParam String justification) {
        try {
            MouvementStockDTO mouvement = mouvementService.enregistrerTransfert(
                    stockId, quantite, locSource, locDest, justification
            );
            return ResponseEntity.ok(Map.of("message", "✅ Transfert enregistré", "mouvement", mouvement));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔗 Lier mouvement à ticket
     */
    @PutMapping("/{id}/lier-ticket")
    public ResponseEntity<?> lierATicket(
            @PathVariable Long id,
            @RequestParam String referenceTicket) {
        try {
            mouvementService.lierMouvementATicket(id, referenceTicket);
            return ResponseEntity.ok(Map.of("message", "✅ Mouvement lié au ticket"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📋 Historique mouvements
     */
    @GetMapping("/historique/{stockId}")
    public ResponseEntity<?> getHistorique(@PathVariable Long stockId) {
        return ResponseEntity.ok(Map.of(
                "mouvements", mouvementService.getHistoriqueMouvements(stockId)
        ));
    }

    /**
     * 📊 Mouvements entre dates
     */
    @GetMapping("/period")
    public ResponseEntity<?> getMouvementsPeriod(
            @RequestParam LocalDateTime debut,
            @RequestParam LocalDateTime fin) {
        return ResponseEntity.ok(Map.of(
                "mouvements", mouvementService.getMouvementsBetweenDates(debut, fin)
        ));
    }
}