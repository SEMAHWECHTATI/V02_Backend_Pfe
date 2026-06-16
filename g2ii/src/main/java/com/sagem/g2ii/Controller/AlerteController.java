package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.AlerteDTO;
import com.sagem.g2ii.Service.AlerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 🎯 AlerteController
 * Gestion et distribution des alertes de stock et de maintenance (GITI-KPI)
 */
@RestController
@RequestMapping("/api/inventory/alertes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class AlerteController {

    private final AlerteService alerteService;

    /**
     * ⚠️ Récupérer toutes les alertes non traitées
     */
    @GetMapping("/non-traitees")
    public ResponseEntity<?> getAlerteNonTraitees() {
        try {
            log.info("📋 GET /api/inventory/alertes/non-traitees");
            var alertes = alerteService.getAlerteNonTraitees();

            return ResponseEntity.ok(Map.of(
                    "total", alertes.size(),
                    "alertes", alertes
            ));
        } catch (Exception e) {
            log.error("❌ Erreur récupération alertes non traitées:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 🔴 Récupérer toutes les alertes critiques
     */
    @GetMapping("/critiques")
    public ResponseEntity<?> getAlerteCritique() {
        try {
            log.info("🔴 GET /api/inventory/alertes/critiques");
            var alertes = alerteService.getAlerteCritique();

            return ResponseEntity.ok(Map.of(
                    "total", alertes.size(),
                    "alertes", alertes
            ));
        } catch (Exception e) {
            log.error("❌ Erreur récupération alertes critiques:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 👁️ Marquer une alerte comme lue (Acquittement visuel)
     */
    @PutMapping("/{id}/marquer-lue")
    public ResponseEntity<?> marquerCommeLue(@PathVariable Long id) {
        try {
            log.info("👁️ PUT /api/inventory/alertes/{}/marquer-lue", id);
            alerteService.marquerCommeLue(id);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Alerte marquée comme lue",
                    "alerteId", id
            ));
        } catch (Exception e) {
            log.error("❌ Erreur marquage alerte comme lue:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * ✅ Marquer une alerte comme traitée (Résolution de l'incident)
     */
    @PutMapping("/{id}/marquer-traitee")
    public ResponseEntity<?> marquerCommeTraitee(@PathVariable Long id) {
        try {
            log.info("✅ PUT /api/inventory/alertes/{}/marquer-traitee", id);
            alerteService.marquerCommeTraitee(id);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Alerte marquée comme traitée",
                    "alerteId", id
            ));
        } catch (Exception e) {
            log.error("❌ Erreur marquage alerte comme traitée:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 📊 Tableau de bord global des alertes
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            log.info("📊 GET /api/inventory/alertes/dashboard");

            var nonTraitees = alerteService.getAlerteNonTraitees();
            var critiques = alerteService.getAlerteCritique();

            Map<String, Object> response = new HashMap<>();
            response.put("totalNonTraitees", nonTraitees.size());
            response.put("totalCritiques", critiques.size());
            response.put("alertesNonTraitees", nonTraitees);
            response.put("alertesCritiques", critiques);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erreur récupération dashboard:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 🔍 Obtenir une alerte par son ID (Complété 🛠️)
     */

    @GetMapping("/{id}")
    public ResponseEntity<?> getAlerteById(@PathVariable Long id) {
        try {
            log.info("🔍 GET /api/inventory/alertes/{}", id);

            // 🚀 Appel direct au service qui cherche en BDD peu importe le statut de l'alerte
            AlerteDTO alerte = alerteService.getAlerteById(id);

            return ResponseEntity.ok(alerte);
        } catch (Exception e) {
            log.error("❌ Erreur récupération alerte ID {}:", id, e);
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 📈 Statistiques des alertes pour les widgets KPI
     */
    @GetMapping("/stats/resume")
    public ResponseEntity<?> getStatistiques() {
        try {
            log.info("📈 GET /api/inventory/alertes/stats/resume");

            var nonTraitees = alerteService.getAlerteNonTraitees();
            var critiques = alerteService.getAlerteCritique();

            int total = nonTraitees.size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAlertesActives", total);
            stats.put("nonTraitees", nonTraitees.size());
            stats.put("critiques", critiques.size());
            stats.put("pourcentageCritique",
                    total == 0 ? 0 : Math.round((critiques.size() * 100.0) / total));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Erreur récupération statistiques des alertes:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 🗑️ Supprimer une alerte définitivement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerAlerte(@PathVariable Long id) {
        try {
            log.info("🗑️ DELETE /api/inventory/alertes/{}", id);
            alerteService.supprimerAlerte(id);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Alerte supprimée avec succès de l'historique",
                    "alerteId", id
            ));
        } catch (Exception e) {
            log.error("❌ Erreur suppression alerte ID {}:", id, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }
}