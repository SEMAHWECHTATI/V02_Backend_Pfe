package com.sagem.g2ii.Controller;



import com.sagem.g2ii.Service.AlerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 🎯 AlerteController
 * Gestion des alertes de stock
 */
@RestController
@RequestMapping("/api/inventory/alertes")
@RequiredArgsConstructor  // ✅ IMPORTANT : Injection de dépendances
@Slf4j
@CrossOrigin("*")
public class AlerteController {

    private final AlerteService alerteService;  // ✅ IMPORTANT : Déclaration finale

    /**
     * ⚠️ Récupérer toutes les alertes non traitées
     */
    @GetMapping("/non-traitees")
    public ResponseEntity<?> getAlerteNonTraitees() {
        try {
            log.info("📋 GET /alertes/non-traitees");
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
            log.info("🔴 GET /alertes/critiques");
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
     * 👁️ Marquer une alerte comme lue
     */
    @PutMapping("/{id}/marquer-lue")
    public ResponseEntity<?> marquerCommeLue(@PathVariable Long id) {
        try {
            log.info("👁️ PUT /alertes/{}/marquer-lue", id);
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
     * ✅ Marquer une alerte comme traitée
     */
    @PutMapping("/{id}/marquer-traitee")
    public ResponseEntity<?> marquerCommeTraitee(@PathVariable Long id) {
        try {
            log.info("✅ PUT /alertes/{}/marquer-traitee", id);
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
     * 📊 Tableau de bord des alertes
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            log.info("📊 GET /alertes/dashboard");

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
     * 🔍 Obtenir alerte par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAlerteById(@PathVariable Long id) {
        try {
            log.info("🔍 GET /alertes/{}", id);
            // À implémenter dans le service si nécessaire
            return ResponseEntity.ok(Map.of("message", "Alerte récupérée"));
        } catch (Exception e) {
            log.error("❌ Erreur récupération alerte:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 📈 Statistiques des alertes
     */
    @GetMapping("/stats/resume")
    public ResponseEntity<?> getStatistiques() {
        try {
            log.info("📈 GET /alertes/stats/resume");

            var nonTraitees = alerteService.getAlerteNonTraitees();
            var critiques = alerteService.getAlerteCritique();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAlertes", nonTraitees.size() + critiques.size());
            stats.put("nonTraitees", nonTraitees.size());
            stats.put("critiques", critiques.size());
            stats.put("pourcentageCritique",
                    critiques.isEmpty() ? 0 :
                            Math.round((critiques.size() * 100.0) / nonTraitees.size()));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Erreur récupération statistiques:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * 🗑️ Supprimer une alerte
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerAlerte(@PathVariable Long id) {
        try {
            log.info("🗑️ DELETE /alertes/{}", id);
            // À implémenter dans AlerteService
            alerteService.supprimerAlerte(id);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Alerte supprimée",
                    "alerteId", id
            ));
        } catch (Exception e) {
            log.error("❌ Erreur suppression alerte:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }
}