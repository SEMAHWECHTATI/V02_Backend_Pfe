package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.MouvementRequeteDTO;
import com.sagem.g2ii.DTOs.MouvementStockDTO;
import com.sagem.g2ii.DTOs.TransfertRequeteDTO;
import com.sagem.g2ii.Service.MouvementStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/mouvements")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class MouvementStockController {

    private final MouvementStockService mouvementService;



    /**
     * 📥 Enregistrer une entrée de stock
     */
    @PostMapping("/entree")
    public ResponseEntity<?> enregistrerEntree(@RequestBody MouvementRequeteDTO requete) {
        try {
            log.info("Requête de type ENTREE pour le stock ID: {} par l'utilisateur ID: {}", requete.getStockId(), requete.getResponsableId());
            MouvementStockDTO mouvement = mouvementService.enregistrerEntree(
                    requete.getStockId(),
                    requete.getQuantite(),
                    requete.getJustification(),
                    requete.getResponsableId(),    // 👈 Ajouté au service
                    requete.getReferenceTicket()   // 👈 Ajouté au service
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "✅ Entrée enregistrée avec succès", "mouvement", mouvement));
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'entrée: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📤 Enregistrer une sortie de stock
     */
    @PostMapping("/sortie")
    public ResponseEntity<?> enregistrerSortie(@RequestBody MouvementRequeteDTO requete) {
        try {
            log.info("Requête de type SORTIE pour le stock ID: {}", requete.getStockId());
            MouvementStockDTO mouvement = mouvementService.enregistrerSortie(
                    requete.getStockId(),
                    requete.getQuantite(),
                    requete.getJustification(),
                    requete.getResponsableId(),    // 👈 Ajouté au service
                    requete.getReferenceTicket()   // 👈 Ajouté au service
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "✅ Sortie enregistrée avec succès", "mouvement", mouvement));
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de la sortie: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔄 Enregistrer un transfert de stock
     */
    @PostMapping("/transfert")
    public ResponseEntity<?> enregistrerTransfert(@RequestBody TransfertRequeteDTO requete) {
        try {
            log.info("Requête de type TRANSFERT pour le stock ID: {}", requete.getStockId());
            MouvementStockDTO mouvement = mouvementService.enregistrerTransfert(
                    requete.getStockId(),
                    requete.getQuantite(),
                    requete.getLocSource(),
                    requete.getLocDest(),
                    requete.getJustification(),
                    requete.getResponsableId(),    // 👈 Ajouté au service
                    requete.getReferenceTicket()   // 👈 Ajouté au service
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "✅ Transfert enregistré avec succès", "mouvement", mouvement));
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du transfert: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔗 Lier un mouvement existant à un ticket d'incident (GMAO / Helpdesk)
     */
    @PutMapping("/{id}/lier-ticket")
    public ResponseEntity<?> lierATicket(
            @PathVariable Long id,
            @RequestParam String referenceTicket) {
        try {
            log.info("Liaison du mouvement ID: {} au ticket: {}", id, referenceTicket);
            mouvementService.lierMouvementATicket(id, referenceTicket);
            return ResponseEntity.ok(Map.of("message", "✅ Mouvement lié au ticket avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la liaison du ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📋 Historique des mouvements pour un stock spécifique
     */
    @GetMapping("/historique/{stockId}")
    public ResponseEntity<List<MouvementStockDTO>> getHistorique(@PathVariable Long stockId) {
        log.info("Récupération de l'historique pour le stock ID: {}", stockId);
        List<MouvementStockDTO> historique = mouvementService.getHistoriqueMouvements(stockId);
        return ResponseEntity.ok(historique);
    }

    /**
     * 📊 Récupérer les mouvements globaux entre deux dates (Rapport / Audit)
     * Exemple de format attendu dans l'URL : ?debut=2026-05-01T00:00:00&fin=2026-05-31T23:59:59
     */
    @GetMapping("/period")
    public ResponseEntity<List<MouvementStockDTO>> getMouvementsPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("Extraction du rapport des mouvements entre {} et {}", debut, fin);
        List<MouvementStockDTO> mouvements = mouvementService.getMouvementsBetweenDates(debut, fin);
        return ResponseEntity.ok(mouvements);
    }
}