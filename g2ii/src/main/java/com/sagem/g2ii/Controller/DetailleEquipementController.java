package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.EquipementRequestDto;
import com.sagem.g2ii.DTOs.EquipementResponseDto; // 👈 Nouveau DTO de réponse
import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Inventaire.Equipement;
import com.sagem.g2ii.Service.DetailleEquipementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetailleEquipementController {

    private final DetailleEquipementService detailleEquipementService;


    @GetMapping
    public ResponseEntity<List<EquipementResponseDto>> getTousLesEquipements() {
        // Appelle la méthode du service (assurez-vous qu'elle existe, ex: listerTous())
        List<Equipement> equipements = detailleEquipementService.listerTous();
        List<EquipementResponseDto> dtos = equipements.stream()
                .map(this::mapperEnDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEquipmentStatistics() {
        long total = detailleEquipementService.compterTotal();
        long actifs = detailleEquipementService.compterParStatut(StatutArticle.ACTIF);
        long enReparation = detailleEquipementService.compterParStatut(StatutArticle.EN_REPARATION);
        long aRecycler = detailleEquipementService.compterParStatut(StatutArticle.A_RECYCLER);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEquipements", total);
        stats.put("actifs", actifs);
        stats.put("enReparation", enReparation);
        stats.put("aRecycler", aRecycler);
        stats.put("tauxActivite", total > 0 ? Math.round((double) actifs / total * 100) : 0);

        return ResponseEntity.ok(stats);
    }
    /**
     * 💡 Méthode utilitaire privée pour transformer proprement une entité Equipement
     * contenant des relations cycliques en un DTO linéaire et sécurisé.
     */
    private EquipementResponseDto mapperEnDto(Equipement eq) {
        if (eq == null) return null;

        EquipementResponseDto dto = new EquipementResponseDto();
        dto.setId(eq.getId());
        dto.setCodeBarres(eq.getCodeBarres());
        dto.setDesignation(eq.getDesignation());
        dto.setStatut(eq.getStatut());

        // Extraction des informations de l'article sans remonter sa liste d'équipements
        if (eq.getArticle() != null) {
            dto.setArticleId(eq.getArticle().getId());
            dto.setArticleReference(eq.getArticle().getReference());
            dto.setArticleDesignation(eq.getArticle().getDesignation());
        }

        // Extraction des informations de localisation si elle existe
        if (eq.getLocalisation() != null) {
            dto.setLocalisationId(eq.getLocalisation().getId());
            dto.setLocalisationNom(eq.getLocalisation().getNom());
            dto.setLocalisationBatiment(eq.getLocalisation().getBatiment()); // 👈 Ajouter
            dto.setLocalisationEtage(eq.getLocalisation().getEtage());       // 👈 Ajouter
            dto.setLocalisationBureau(eq.getLocalisation().getBureau());// Adaptez selon votre champ (ex: getNom(), getSalle()...)
        }

        return dto;
    }

    /**
     * GET /api/equipements/{id} : Consulter les détails d'un équipement
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipementResponseDto> getEquipementDetails(@PathVariable Long id) {
        Equipement eq = detailleEquipementService.obtenirDetailsEquipement(id);
        return ResponseEntity.ok(mapperEnDto(eq));
    }

    /**
     * GET /api/equipements/scan/{codeBarres} : Chercher un équipement par Code-barres
     */
    @GetMapping("/scan/{codeBarres}")
    public ResponseEntity<EquipementResponseDto> getEquipementParCodeBarres(@PathVariable String codeBarres) {
        Equipement eq = detailleEquipementService.obtenirParCodeBarres(codeBarres);
        return ResponseEntity.ok(mapperEnDto(eq));
    }

    /**
     * POST /api/equipements : Ajouter un équipement
     */
    @PostMapping
    public ResponseEntity<?> ajouterEquipement(@RequestBody EquipementRequestDto dto) {
        Equipement nouvelEquipement = detailleEquipementService.creerEquipement(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("id", nouvelEquipement.getId());
        response.put("codeBarres", nouvelEquipement.getCodeBarres());
        response.put("message", "Équipement créé avec succès !");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/equipements/{id}/statut-localisation : Modifier le statut et l'emplacement
     */
    @PatchMapping("/{id}/statut-localisation")
    public ResponseEntity<EquipementResponseDto> modifierStatutEtLocalisation(
            @PathVariable Long id,
            @RequestParam(required = false) StatutArticle statut,
            @RequestParam(required = false) Long localisationId) {

        Equipement equipementMisAJour = detailleEquipementService.mettreAJourStatutEtLocalisation(id, statut, localisationId);
        return ResponseEntity.ok(mapperEnDto(equipementMisAJour));
    }

    /**
     * GET /api/equipements/statut/{statut} : Filtrer par statut (ACTIF, EN_REPARATION, A_RECYCLER)
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<EquipementResponseDto>> getEquipementsParStatut(@PathVariable StatutArticle statut) {
        List<Equipement> equipements = detailleEquipementService.listerParStatut(statut);
        List<EquipementResponseDto> dtos = equipements.stream()
                .map(this::mapperEnDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/equipements/localisation/{localisationId} : Filtrer par emplacement physique
     */
    @GetMapping("/localisation/{localisationId}")
    public ResponseEntity<List<EquipementResponseDto>> getEquipementsParLocalisation(@PathVariable Long localisationId) {
        List<Equipement> equipements = detailleEquipementService.listerParLocalisation(localisationId);
        List<EquipementResponseDto> dtos = equipements.stream()
                .map(this::mapperEnDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}