package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.ArticleDTO;
import com.sagem.g2ii.DTOs.DemandeMaterielDTO;
import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Entity.Inventaire.DemandeMateriel;
import com.sagem.g2ii.Service.DemandeMaterielService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes-materiel")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class DemandeMaterielController {

    private final DemandeMaterielService demandeService;

    /**
     * 📝 Créer une demande de matériel
     * POST /api/demandes-materiel?utilisateurId=1
     */
    @PostMapping
    public ResponseEntity<?> creerDemande(
            @RequestBody DemandeMaterielDTO dto,
            @RequestParam Long utilisateurId) {
        try {
            log.info("📝 POST /demandes-materiel - Création demande pour utilisateur: {}", utilisateurId);
            DemandeMaterielDTO created = demandeService.creerDemande(dto, utilisateurId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "✅ Demande créée avec succès",
                    "demande", created
            ));
        } catch (Exception e) {
            log.error("❌ Erreur création demande:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Valider demande par Gestionnaire
     * PUT /api/demandes-materiel/{id}/valider-gestionnaire?gestionnaireId=2
     */
    @PutMapping("/{id}/valider-gestionnaire")
    public ResponseEntity<?> validerParGestionnaire(
            @PathVariable Long id,
            @RequestParam Long gestionnaireId) {
        try {
            log.info("✅ PUT /demandes-materiel/{}/valider-gestionnaire - Gestionnaire: {}", id, gestionnaireId);
            DemandeMaterielDTO updated = demandeService.validerParGestionnaire(id, gestionnaireId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✅ Demande validée par gestionnaire (En attente d'approbation admin)",
                    "demande", updated
            ));
        } catch (Exception e) {
            log.error("❌ Erreur validation gestionnaire:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Valider demande par Admin (CONSOMME LE STOCK)
     * PUT /api/demandes-materiel/{id}/valider-admin?adminId=3
     */
    @PutMapping("/{id}/valider-admin")
    public ResponseEntity<?> validerParAdmin(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        try {
            log.info("✅ PUT /demandes-materiel/{}/valider-admin - Admin: {}", id, adminId);
            DemandeMaterielDTO updated = demandeService.validerParAdmin(id, adminId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✅ Demande validée par admin - Stock consommé automatiquement",
                    "demande", updated,
                    "stock_consumed", Map.of(
                            "article", updated.getArticleDesignation(),
                            "quantity", updated.getQuantiteDemandee(),
                            "date_consumption", updated.getDateConsommation()
                    )
            ));
        } catch (Exception e) {
            log.error("❌ Erreur validation admin:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ❌ Rejeter une demande
     * PUT /api/demandes-materiel/{id}/rejeter?validateurId=2&motifRejet=Stock insuffisant
     */
    @PutMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterDemande(
            @PathVariable Long id,
            @RequestParam Long validateurId,
            @RequestParam String motifRejet) {
        try {
            log.info("❌ PUT /demandes-materiel/{}/rejeter - Motif: {}", id, motifRejet);
            DemandeMaterielDTO updated = demandeService.rejeterDemande(id, validateurId, motifRejet);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✅ Demande rejetée",
                    "demande", updated
            ));
        } catch (Exception e) {
            log.error("❌ Erreur rejet demande:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 📋 Récupérer demandes en attente (Pour Gestionnaire)
     * GET /api/demandes-materiel/en-attente
     */
    @GetMapping("/en-attente")
    public ResponseEntity<?> getDemandesEnAttente() {
        try {
            log.info("📋 GET /demandes-materiel/en-attente");
            List<DemandeMaterielDTO> demandes = demandeService.getDemandesEnAttente();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "total", demandes.size(),
                    "statut", "EN_ATTENTE",
                    "demandes", demandes
            ));
        } catch (Exception e) {
            log.error("❌ Erreur récupération demandes en attente:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 📋 Récupérer demandes validées gestionnaire (Pour Admin)
     * GET /api/demandes-materiel/validee-gestionnaire
     */
    @GetMapping("/validee-gestionnaire")
    public ResponseEntity<?> getDemandesValideeGestionnaire() {
        try {
            log.info("📋 GET /demandes-materiel/validee-gestionnaire");
            List<DemandeMaterielDTO> demandes = demandeService.getDemandesValideeGestionnaire();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "total", demandes.size(),
                    "statut", "VALIDE_GESTIONNAIRE",
                    "demandes", demandes
            ));
        } catch (Exception e) {
            log.error("❌ Erreur récupération demandes validées gestionnaire:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 📋 Récupérer mes demandes (Par utilisateur)
     * GET /api/demandes-materiel/mes-demandes?utilisateurId=1
     */
    @GetMapping("/mes-demandes")
    public ResponseEntity<?> getMesDemandes(@RequestParam Long utilisateurId) {
        try {
            log.info("📋 GET /demandes-materiel/mes-demandes - Utilisateur: {}", utilisateurId);
            List<DemandeMaterielDTO> demandes = demandeService.getDemanduesUtilisateur(utilisateurId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "total", demandes.size(),
                    "utilisateur_id", utilisateurId,
                    "demandes", demandes
            ));
        } catch (Exception e) {
            log.error("❌ Erreur récupération mes demandes:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 📊 Statistiques des demandes
     * GET /api/demandes-materiel/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistiques() {
        try {
            log.info("📊 GET /demandes-materiel/stats");

            List<DemandeMaterielDTO> enAttente = demandeService.getDemandesEnAttente();
            List<DemandeMaterielDTO> valideeGestionnaire = demandeService.getDemandesValideeGestionnaire();

            Map<String, Object> stats = new HashMap<>();
            stats.put("en_attente", enAttente.size());
            stats.put("validee_gestionnaire", valideeGestionnaire.size());
            stats.put("total_en_cours", enAttente.size() + valideeGestionnaire.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "📊 Statistiques des demandes",
                    "statistics", stats
            ));
        } catch (Exception e) {
            log.error("❌ Erreur statistiques:", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }


}