package com.sagem.g2ii.Controller;

// NOUVEAUX IMPORTS : On importe les DTOs au lieu de l'Entité
import com.sagem.g2ii.DTOs.ApprobationDTO;
import com.sagem.g2ii.DTOs.DemandeCreationDTO;
import com.sagem.g2ii.DTOs.DemandeReponseDTO;
import com.sagem.g2ii.Entity.Authentification.DemandeInscription;
import com.sagem.g2ii.Service.DemandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
public class DemandeInscrController {

    @Autowired
    private DemandeService demandeService;

    // 1. Endpoint pour le Demandeur : Envoyer une nouvelle demande
    @PostMapping("/envoyer")
    public ResponseEntity<DemandeReponseDTO> creerDemande(@RequestBody DemandeCreationDTO dto) {
        // Le contrôleur reçoit un DTO entrant et renvoie un DTO sortant
        DemandeReponseDTO nouvelleDemande = demandeService.creerDemande(dto);
        return new ResponseEntity<>(nouvelleDemande, HttpStatus.CREATED);
    }

    // 2. Endpoint pour l'Admin : Voir toutes les demandes (en attente, approuvées, etc.)
    @GetMapping("/all")
    public ResponseEntity<List<DemandeReponseDTO>> getAllDemandes() {
        // La liste renvoie maintenant des objets propres et allégés
        List<DemandeReponseDTO> demandes = demandeService.getAllDemandes();
        return ResponseEntity.ok(demandes);
    }

    // 3. Endpoint pour l'Admin : Approuver une demande (C'est ici que l'Utilisateur est créé)
    @PutMapping("/approuver/{id}")
    public ResponseEntity<String> approuverDemande(
            @PathVariable Long id,
            @RequestBody ApprobationDTO approbationDTO) { // L'Admin doit maintenant envoyer le rôle et le groupe !
        try {
            demandeService.approuverDemande(id, approbationDTO);
            return ResponseEntity.ok("La demande a été approuvée (Rôle: " + approbationDTO.getRoleAccorde() + "). L'utilisateur a été créé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'approbation : " + e.getMessage());
        }
    }

    // 4. Endpoint pour l'Admin : Refuser une demande avec un motif
    @PutMapping("/refuser/{id}")
    public ResponseEntity<String> refuserDemande(@PathVariable Long id, @RequestParam String motif) {
        demandeService.refuserDemande(id, motif);
        return ResponseEntity.ok("La demande a été refusée.");
    }

    @DeleteMapping("deleteDemandeInscrip/{id}")
    public void deleteDemandeInscrip(@PathVariable Long id) {
        demandeService.deletedemandeInscrip(id);
    }

    @PutMapping("/archiver/{id}")
    public ResponseEntity<?> archiverDemande(@PathVariable Long id) {
        // On appelle la méthode que l'on vient de créer dans le service
        demandeService.archiverDemande(id);

        // On renvoie le message de succès au format HTTP (pour Angular)
        return ResponseEntity.ok().body("Demande archivée avec succès !");
    }
    // 1. Obtenir les demandes actives
    @GetMapping("/actives")
    public List<DemandeInscription> getDemandesActives() {
        return demandeService.getDemandesActives();
    }

    // 2. Obtenir les demandes archivées
    @GetMapping("/archives")
    public List<DemandeInscription> getDemandesArchivees() {
        return demandeService.getDemandesArchivees();
    }
}