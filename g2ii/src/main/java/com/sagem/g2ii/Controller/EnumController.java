package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Enumeration.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enumerations") // Une URL générique pour vos listes de référence
@CrossOrigin("*")
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

    @GetMapping("/Priorite")
    public ResponseEntity<Priorite[]> getPrioriter() {
        return ResponseEntity.ok(Priorite.values());
    }

    @GetMapping("/TypeTicket")
    public ResponseEntity<TypeTicket[]> getTypeTicket() {
        return ResponseEntity.ok(TypeTicket.values());
    }

    @GetMapping("/StatutTicket")
    public ResponseEntity<StatutTicket[]> getStatutTicket() {
        return ResponseEntity.ok(StatutTicket.values());
    }

    @GetMapping("/TypeNote")
    public ResponseEntity<TypeNote[]> getTypeNote() {
        return ResponseEntity.ok(TypeNote.values());
    }
}
