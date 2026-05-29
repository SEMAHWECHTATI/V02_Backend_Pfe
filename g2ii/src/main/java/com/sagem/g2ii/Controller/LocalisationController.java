package com.sagem.g2ii.Controller;


import com.sagem.g2ii.DTOs.LocalisationDTO;
import com.sagem.g2ii.Service.LocalisationMatrielService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localisations")
@RequiredArgsConstructor
@CrossOrigin("*") // Permet à Angular de requêter l'API sans blocage CORS
public class LocalisationController {

    private final LocalisationMatrielService localisationService;

    /**
     * 📥 GET : Récupérer toutes les localisations
     * URL : http://localhost:8080/api/localisations
     */
    @GetMapping
    public ResponseEntity<List<LocalisationDTO>> getAllLocalisations() {
        List<LocalisationDTO> localisations = localisationService.getAllLocalisations();
        return ResponseEntity.ok(localisations);
    }

    /**
     * 📥 GET : Récupérer une localisation par son ID
     * URL : http://localhost:8080/api/localisations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocalisationDTO> getLocalisationById(@PathVariable Long id) {
        try {
            LocalisationDTO localisation = localisationService.getLocalisationById(id);
            return ResponseEntity.ok(localisation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * 📤 POST : Créer une nouvelle localisation
     */
    @PostMapping // 💡 Remplace @HttpPost par @PostMapping
    public ResponseEntity<?> creerLocalisation(@RequestBody LocalisationDTO localisationDTO) {
        try {
            LocalisationDTO nouvelleLocalisation = localisationService.creerLocalisation(localisationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleLocalisation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Erreur de création : " + e.getMessage());
        }
    }

    /**
     * 🔄 PUT : Modifier une localisation existante
     * URL : http://localhost:8080/api/localisations/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierLocalisation(@PathVariable Long id, @RequestBody LocalisationDTO localisationDTO) {
        try {
            LocalisationDTO localisationModifiee = localisationService.modifierLocalisation(id, localisationDTO);
            return ResponseEntity.ok(localisationModifiee);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Erreur de modification : " + e.getMessage());
        }
    }

    /**
     * ❌ DELETE : Supprimer définitivement une localisation
     * URL : http://localhost:8080/api/localisations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerLocalisation(@PathVariable Long id) {
        try {
            localisationService.supprimerLocalisation(id);
            return ResponseEntity.ok("✅ Localisation supprimée avec succès.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}