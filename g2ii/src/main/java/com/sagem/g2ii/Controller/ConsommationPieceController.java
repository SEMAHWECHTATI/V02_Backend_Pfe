package com.sagem.g2ii.Controller;
import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import com.sagem.g2ii.Service.ConsommationPieceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/consommations-pieces")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // À ajuster selon l'URL de votre front Angular
public class ConsommationPieceController {

    private final ConsommationPieceServiceImpl consommationPieceService;

    /**
     * POST : Enregistrer une nouvelle consommation de pièce (Ex: lors de la résolution)
     */
    @PostMapping
    public ResponseEntity<ConsommationPiece> creerConsommation(@RequestBody ConsommationPiece consommation) {
        ConsommationPiece nouvelleConsommation = consommationPieceService.enregistrerConsommation(consommation);
        return new ResponseEntity<>(nouvelleConsommation, HttpStatus.CREATED);
    }

    /**
     * GET : Récupérer toutes les pièces consommées sur un ticket spécifique
     */
    @GetMapping("/ticket/{reference}")
    public ResponseEntity<List<ConsommationPiece>> getConsommationsParTicket(@PathVariable("reference") String referenceTicket) {
        List<ConsommationPiece> consommations = consommationPieceService.obtenirConsommationsParTicket(referenceTicket);
        return ResponseEntity.ok(consommations);
    }

    /**
     * GET : Liste globale de toutes les consommations (pour des rapports/inventaires)
     */
    @GetMapping
    public ResponseEntity<List<ConsommationPiece>> getToutesLesConsommations() {
        return ResponseEntity.ok(consommationPieceService.obtenirToutesLesConsommations());
    }

    /**
     * DELETE : Annuler/Supprimer une consommation par son ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerConsommation(@PathVariable Long id) {
        consommationPieceService.annulerConsommation(id);
        return ResponseEntity.noContent().build();
    }
}
