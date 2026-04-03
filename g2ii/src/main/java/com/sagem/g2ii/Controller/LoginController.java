package com.sagem.g2ii.Controller;


import com.sagem.g2ii.DTOs.ChangerMotDePasseRequest;
import com.sagem.g2ii.DTOs.LoginRequest;
import com.sagem.g2ii.DTOs.LoginResponse;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Service.AuthentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/authentification")
@CrossOrigin( "*") // Angular dev server
public class LoginController {
    @Autowired
    private PasswordEncoder encoder; // ✅ Spring trouvera le bean que vous avez défini !

    @Autowired
    private AuthentificationService autheService;

    @Autowired
    private IntUtilisateur utilisateurRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return utilisateurRepo.findByEmail(request.getEmail())
                .map(u -> {
                    boolean ok = encoder.matches(request.getMotDePasse(), u.getMotDePasse());
                    if (!ok) return ResponseEntity.status(401).body("Email ou mot de passe incorrect");

                    if (u.isMotDepassetemporaire()) {
                        return ResponseEntity.status(200)
                                .body("CHANGER_MDP"); // Signal pour le frontend
                    }

                    // --- NOUVEAU CODE ICI ---
                    // On construit la réponse avec les informations de l'utilisateur (u)
                    LoginResponse reponse = LoginResponse.builder()
                            .token("dummy-token-si-pas-encore-configure") // À remplacer par un vrai token plus tard si besoin
                            .nom(u.getNom())
                            .prenom(u.getPrenom())
                            .role(u.getRole()) // toString() au cas où le rôle est un Enum
                            .departement(u.getDepartement())
                            .email(u.getEmail())
                            .build();

                    return ResponseEntity.ok(reponse);
                })
                .orElse(ResponseEntity.status(401).body("Email ou mot de passe incorrect"));
    }
    @PostMapping("/changer-mdp")
    public ResponseEntity<?> changerMotDePasse(@RequestBody ChangerMotDePasseRequest request) {
        // 1. On appelle le service
        boolean succes = autheService.changerMotDePasse(request.getEmail(), request.getMotDePasse());

        // 2. On prépare la réponse HTTP en fonction du résultat
        if (succes) {
            // Renvoie un JSON : {"message": "Mot de passe modifié avec succès"}
            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
        } else {
            // Renvoie un JSON : {"erreur": "Utilisateur non trouvé"} avec le statut 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erreur", "Utilisateur non trouvé"));
        }
    }
    }


