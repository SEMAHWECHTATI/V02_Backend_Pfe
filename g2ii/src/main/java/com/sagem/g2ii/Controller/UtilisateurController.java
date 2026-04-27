package com.sagem.g2ii.Controller;


import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Service.DemandeService;
import com.sagem.g2ii.Service.UtilisateurService;
import com.sagem.g2ii.securiter.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin( "*")

public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private IntUtilisateur intUtilisateur;

    // Injectez vos services (adaptez les noms selon votre projet)
    @Autowired
    private JwtService jwtService; // Votre service qui gère le JWT (génération/extraction)


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

    @GetMapping("/verifyuser")
    public ResponseEntity<?> verifyUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 1. Vérifier si le header existe et respecte le format "Bearer "
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token manquant ou mal formaté");
            }

            // 2. Extraire le token (on coupe les 7 premiers caractères : "Bearer ")
            String token = authHeader.substring(7);

            // 3. Extraire l'email ou le nom d'utilisateur depuis le token
            // (La méthode extractUsername dépend de comment vous avez codé votre JwtService)
            String email = jwtService.extractUsername(token);

            // 4. Chercher l'utilisateur en base de données
            Optional<Utilisateur> utilisateurOpt = intUtilisateur.findByEmail(email);

            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                // ✅ Succès : On retourne les informations de l'utilisateur
                return ResponseEntity.ok(utilisateur);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Utilisateur introuvable");
            }

        } catch (Exception e) {
            // ❌ Échec : Le token est expiré, falsifié ou invalide
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token invalide ou expiré : " + e.getMessage());
        }
    }

}
