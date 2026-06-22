package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Intervention.SLA;
import com.sagem.g2ii.Service.SLAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permet l'interconnexion complète avec votre Front Angular
public class SLAController {

    private final SLAService slaService;

    @GetMapping
    public ResponseEntity<List<SLA>> getTousLesSLA() {
        return ResponseEntity.ok(slaService.obtenirTousLesSLA());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SLA> getSlaParId(@PathVariable Long id) {
        return ResponseEntity.ok(slaService.obtenirSlaParId(id));
    }

    @GetMapping("/categorie/{idCategorie}")
    public ResponseEntity<List<SLA>> getSlasParCategorie(@PathVariable Long idCategorie) {
        return ResponseEntity.ok(slaService.obtenirSlasParCategorie(idCategorie));
    }

    @PostMapping
    public ResponseEntity<SLA> creerSLA(@RequestBody SLA sla) {
        return new ResponseEntity<>(slaService.enregistrerSLA(sla), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SLA> modifierSLA(@PathVariable Long id, @RequestBody SLA slaDetails) {
        return ResponseEntity.ok(slaService.modifierSLA(id, slaDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerSLA(@PathVariable Long id) {
        slaService.supprimerSLA(id);
        return ResponseEntity.noContent().build();
    }
}