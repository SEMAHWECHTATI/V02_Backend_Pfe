package com.sagem.g2ii.Controller;


import com.sagem.g2ii.DTOs.FournisseurDTO;
import com.sagem.g2ii.Service.FournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // À adapter selon le port de ton frontend (ex: "http://localhost:4200")
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @GetMapping
    public ResponseEntity<List<FournisseurDTO>> getAllFournisseurs() {
        return ResponseEntity.ok(fournisseurService.getAllFournisseurs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurDTO> getFournisseurById(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.getFournisseurById(id));
    }

    @PostMapping
    public ResponseEntity<FournisseurDTO> createFournisseur(@Valid @RequestBody FournisseurDTO fournisseurDTO) {
        FournisseurDTO created = fournisseurService.createFournisseur(fournisseurDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurDTO> updateFournisseur(
            @PathVariable Long id,
            @Valid @RequestBody FournisseurDTO fournisseurDTO) {
        FournisseurDTO updated = fournisseurService.updateFournisseur(id, fournisseurDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFournisseur(@PathVariable Long id) {
        fournisseurService.deleteFournisseur(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage()); // ex.getMessage() contiendra "❌ Un fournisseur avec ce nom existe déjà."

        // On renvoie un statut 400 (Bad Request) ou 409 (Conflict) au lieu d'un 500
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}