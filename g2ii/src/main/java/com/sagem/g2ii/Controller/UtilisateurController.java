package com.sagem.g2ii.Controller;


import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Service.DemandeService;
import com.sagem.g2ii.Service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin( "*")

public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;



    @PostMapping
    public Utilisateur addUser(@RequestBody Utilisateur user) {
        return utilisateurService.addUser(user);
    }

    @GetMapping
    public List<Utilisateur> getAllUsers() {
        return utilisateurService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Utilisateur getUserById(@PathVariable Long id) {
        return utilisateurService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        utilisateurService.deleteUserById(id);
    }

    @PutMapping("/{id}")
    public Utilisateur updateUser(@PathVariable Long id, @RequestBody Utilisateur user) {
        return utilisateurService.updateUser(id, user);
    }

    @PutMapping("/{userId}/groupes/{groupeId}")
    public ResponseEntity<String> assignGroupToUser(@PathVariable Long userId, @PathVariable Long groupeId) {
        utilisateurService.ajouterUtilisateurAuGroupe(userId, groupeId);
        return ResponseEntity.ok("Groupe affecte a l'utilisateur avec succes.");
    }

}
