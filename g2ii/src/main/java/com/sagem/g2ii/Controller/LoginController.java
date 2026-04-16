package com.sagem.g2ii.Controller;

import com.sagem.g2ii.DTOs.*;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Service.AuthentificationService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/authentification")
@CrossOrigin("*") // Angular dev server
public class LoginController {

    @Autowired
    private AuthentificationService autheService;

    @Autowired
    private IntUtilisateur utilisateurRepo;

    // =========================================================
    // 1. CONNEXION
    // =========================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // On vérifie d'abord si l'utilisateur a un mot de passe temporaire
            Optional<Utilisateur> uOpt = utilisateurRepo.findByEmail(request.getEmail());
            if (uOpt.isPresent() && uOpt.get().isMotDepassetemporaire()) {
                // S'il a un mot de passe temporaire, on renvoie directement le signal au Frontend
                return ResponseEntity.status(200).body("CHANGER_MDP");
            }

            // Sinon, on lance la VRAIE authentification sécurisée
            LoginResponse reponse = autheService.authentifier(request);

            return ResponseEntity.ok(reponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("erreur", e.getMessage()));
        }
    }

    // =========================================================
    // 📌 NOUVEAU: RÉCUPÉRER UN UTILISATEUR PAR ID
    // =========================================================
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<Utilisateur> utilisateurOpt = utilisateurRepo.findById(id);

            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                LoginResponse response = mapperUtilisateurVersLoginResponse(utilisateur);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erreur", "Utilisateur non trouvé"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erreur", "Erreur lors de la récupération de l'utilisateur"));
        }
    }

    // =========================================================
    // 2. CHANGEMENT DE MOT DE PASSE (Après 1ère connexion)
    // =========================================================
    @PostMapping("/changer-mdp")
    public ResponseEntity<?> changerMotDePasse(@RequestBody ChangerMotDePasseRequest request) {
        boolean succes = autheService.changerMotDePasse(request.getEmail(), request.getMotDePasse());

        if (succes) {
            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erreur", "Utilisateur non trouvé"));
        }
    }

    // =========================================================
    // 3. DEMANDE DE RÉINITIALISATION (L'utilisateur oublie son mdp)
    // =========================================================
    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<?> demanderReinitialisation(@RequestBody ForgotPasswordDto request) {
        try {
            autheService.demanderReinitialisationMotDePasse(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Si cet email existe, un lien de réinitialisation a été généré."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Une erreur est survenue lors de la demande."));
        }
    }

    // =========================================================
    // 4. VALIDATION DU NOUVEAU MOT DE PASSE VIA LE TOKEN
    // =========================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> reinitialiserMotDePasse(@RequestBody ResetPasswordDto request) {
        try {
            autheService.reinitialiserMotDePasse(request.getToken(), request.getNouveauMotDePasse());
            return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été réinitialisé avec succès."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // =========================================================
    // 📌 MÉTHODE UTILITAIRE: MAPPER UTILISATEUR VERS LOGIN RESPONSE
    // =========================================================
    /**
     * Convertit un Utilisateur vers LoginResponse avec tous les détails
     * incluant les groupes, les préférences, etc.
     */
    private LoginResponse mapperUtilisateurVersLoginResponse(Utilisateur utilisateur) {
        // 📍 Mapper les groupes
        List<LoginResponse.GroupeDTO> groupesDTOs = utilisateur.getGroupes() != null
                ? utilisateur.getGroupes()
                .stream()
                .map(groupe -> LoginResponse.GroupeDTO.builder()
                        .id(groupe.getId())
                        .nomGroupes(groupe.getNomGroupes())
                        .description(groupe.getDescription())
                        .actif(groupe.isActif())
                        .dateCreation(groupe.getDateCreation())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        // 📍 Mapper les préférences (optionnel)
        LoginResponse.PreferencesDTO preferencesDTO = null;
        if (utilisateur.getPreferences() != null) {
            preferencesDTO = LoginResponse.PreferencesDTO.builder()
                    .id(utilisateur.getPreferences().getId())
                    .typeAlerte(utilisateur.getPreferences().getTypeAlerte() != null
                            ? utilisateur.getPreferences().getTypeAlerte().toString()
                            : "INSCRIPTION")
                    .canalEmail(utilisateur.getPreferences().isCanalEmail())
                    .canalInApp(utilisateur.getPreferences().isCanalInApp())
                    .actif(utilisateur.getPreferences().isActif())
                    .build();
        }

        // 🟢 Construire et retourner la LoginResponse
        return LoginResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .matricule(utilisateur.getMatricule())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole())
                .departement(utilisateur.getDepartement())
                .statut(utilisateur.getStatut() != null
                        ? utilisateur.getStatut().toString()
                        : "Actif")
                .groupes(groupesDTOs) // 📌 LES GROUPES !
                .preferences(preferencesDTO)
                .dateDernierConnex(utilisateur.getDate_dernier_Connex())
                .dateCreationCompte(utilisateur.getDate_Creation_Compte())
                .build();
    }
}