package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.DemandeMaterielDTO;
import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Service.DemandeMaterielService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes-materiel")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class DemandeMaterielController {

    private final DemandeMaterielService demandeService;

    /**
     * 📝 Créer une demande
     */
    @PostMapping
    public ResponseEntity<?> creerDemande(
            @RequestBody DemandeMaterielDTO dto,
            @RequestParam Long utilisateurId) {
        try {
            log.info("POST /demandes-materiel - Création demande");
            DemandeMaterielDTO created = demandeService.creerDemande(dto, utilisateurId);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Demande créée avec succès",
                    "demande", created
            ));
        } catch (Exception e) {
            log.error("❌ Erreur création demande:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Valider par Gestionnaire
     */
    @PutMapping("/{id}/valider-gestionnaire")
    public ResponseEntity<?> validerParGestionnaire(
            @PathVariable Long id,
            @RequestParam Long gestionnaireId) {
        try {
            log.info("PUT /demandes-materiel/{}/valider-gestionnaire", id);
            DemandeMaterielDTO updated = demandeService.validerParGestionnaire(id, gestionnaireId);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Demande validée par gestionnaire",
                    "demande", updated
            ));
        } catch (Exception e) {
            log.error("❌ Erreur validation gestionnaire:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Valider par Admin (CONSOMMATION AUTO)
     */
    @PutMapping("/{id}/valider-admin")
    public ResponseEntity<?> validerParAdmin(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        try {
            log.info("PUT /demandes-materiel/{}/valider-admin", id);
            DemandeMaterielDTO updated = demandeService.validerParAdmin(id, adminId);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Demande validée et stock consommé",
                    "demande", updated
            ));
        } catch (Exception e) {
            log.error("❌ Erreur validation admin:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ❌ Rejeter une demande
     */
    @PutMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterDemande(
            @PathVariable Long id,
            @RequestParam Long validateurId,
            @RequestParam String motifRejet) {
        try {
            log.info("PUT /demandes-materiel/{}/rejeter", id);
            DemandeMaterielDTO updated = demandeService.rejeterDemande(id, validateurId, motifRejet);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Demande rejetée",
                    "demande", updated
            ));
        } catch (Exception e) {
            log.error("❌ Erreur rejet demande:", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📋 Demandes en attente (Pour Gestionnaire)
     */
    @GetMapping("/en-attente")
    public ResponseEntity<?> getDemandesEnAttente() {
        try {
            log.info("GET /demandes-materiel/en-attente");
            var demandes = demandeService.getDemandesEnAttente();

            return ResponseEntity.ok(Map.of(
                    "total", demandes.size(),
                    "demandes", demandes
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📋 Demandes validées gestionnaire (Pour Admin)
     */
    @GetMapping("/validee-gestionnaire")
    public ResponseEntity<?> getDemandesValideeGestionnaire() {
        try {
            log.info("GET /demandes-materiel/validee-gestionnaire");
            var demandes = demandeService.getDemandesValideeGestionnaire();

            return ResponseEntity.ok(Map.of(
                    "total", demandes.size(),
                    "demandes", demandes
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📋 Mes demandes
     */
    @GetMapping("/mes-demandes")
    public ResponseEntity<?> getMesDemandes(@RequestParam Long utilisateurId) {
        try {
            log.info("GET /demandes-materiel/mes-demandes");
            var demandes = demandeService.getDemanduesUtilisateur(utilisateurId);

            return ResponseEntity.ok(Map.of(
                    "total", demandes.size(),
                    "demandes", demandes
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}