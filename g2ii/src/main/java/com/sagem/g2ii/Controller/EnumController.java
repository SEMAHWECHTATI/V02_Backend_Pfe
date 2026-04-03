package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Enumeration.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/references") // Une URL générique pour vos listes de référence
public class EnumController {

    // 1. Endpoint pour récupérer les rôles
    @GetMapping("/roles")
    public ResponseEntity<roleUtilisateur[]> getRoles() {
        // .values() renvoie automatiquement ["Administrateur", "Technicien", ...]
        return ResponseEntity.ok(roleUtilisateur.values());
    }

    // 2. Endpoint pour récupérer les départements
    @GetMapping("/departements")
    public ResponseEntity<departementService[]> getDepartements() {
        return ResponseEntity.ok(departementService.values());
    }

    @GetMapping("/actionAudit")
    public ResponseEntity<ActionAudit[]> getActionAudit() {
        return ResponseEntity.ok(ActionAudit.values());
    }

    @GetMapping("/groupeTechnicien")
    public ResponseEntity<GroupeTechnicien[]> getGroupeTechnicien() {
        return ResponseEntity.ok(GroupeTechnicien.values());
    }

    @GetMapping("/statutDemandeInscri")
    public ResponseEntity<statutDemandeInscription[]> getStatutDemandeInscri() {
        return ResponseEntity.ok(statutDemandeInscription.values());
    }

    @GetMapping("/Typealerte")
    public ResponseEntity<TypeAlerte[]> getTypealerte() {
        return ResponseEntity.ok(TypeAlerte.values());
    }

    @GetMapping("/statutUtilisateur")
    public ResponseEntity<statutUtilisateur[]> getStatutUtilisateur() {
        return ResponseEntity.ok(statutUtilisateur.values());
    }
}
