package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.LoginRequest;
import com.sagem.g2ii.DTOs.LoginResponse;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.statutUtilisateur;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.securiter.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthentificationService {
    private final IntUtilisateur utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JournalAuditService journalAuditService;
    private final EmailService emailService;

    public LoginResponse authentifier(LoginRequest request) {

        // 1. Chercher l'utilisateur par son email
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 2. Vérifier si le compte est bloqué DÉFINITIVEMENT par l'Admin
        if (user.getStatut() == statutUtilisateur.Bloque) {
            journalAuditService.enregistrerLog(ActionAudit.ECHEC_CONNEXION, "Tentative de connexion sur un compte bloqué définitivement", user);
            throw new RuntimeException("Votre compte a été bloqué par un administrateur. Veuillez le contacter.");
        }

        // 3. Vérification du blocage TEMPORAIRE (5 minutes)
        if (user.getCompteBloqueJusqua() != null) {
            if (user.getCompteBloqueJusqua().isAfter(LocalDateTime.now())) {
                journalAuditService.enregistrerLog(ActionAudit.ECHEC_CONNEXION, "Tentative de connexion pendant le blocage temporaire (5 min)", user);
                throw new RuntimeException("Compte temporairement bloqué suite à plusieurs échecs. Réessayez plus tard.");
            } else {
                user.setCompteBloqueJusqua(null);
                user.setTentative_login(0);
                utilisateurRepository.save(user);
            }
        }

        // 4. Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getMotDePasse(), user.getMotDePasse())) {

            int tentatives = (user.getTentative_login() == null ? 0 : user.getTentative_login()) + 1;
            user.setTentative_login(tentatives);

            if (tentatives >= 3) {
                user.setCompteBloqueJusqua(LocalDateTime.now().plusMinutes(2));
                utilisateurRepository.save(user);

                journalAuditService.enregistrerLog(ActionAudit.BLOCAGE, "Compte bloqué pour 5 minutes suite à 3 tentatives infructueuses", user);
                throw new RuntimeException("Compte bloqué pour 5 minutes : 3 tentatives infructueuses.");
            }

            utilisateurRepository.save(user);
            journalAuditService.enregistrerLog(ActionAudit.ECHEC_CONNEXION, "Mot de passe incorrect (" + tentatives + "/3)", user);
            throw new RuntimeException("Mot de passe incorrect (" + tentatives + "/3).");
        }

        // 5. ✅ SUCCÈS : Le mot de passe est bon !
        user.setTentative_login(0);
        user.setDate_dernier_Connex(LocalDateTime.now());
        utilisateurRepository.save(user);

        journalAuditService.enregistrerLog(ActionAudit.CONNEXION, "Connexion réussie", user);

        // 6. Générer le Token JWT
        String jwtToken = jwtService.generateToken(user);

        // 7. 📌 Construire la réponse COMPLÈTE avec tous les détails
        return construireLoginResponse(user, jwtToken);
    }

    /**
     * 📌 Construit une LoginResponse complète avec tous les détails de l'utilisateur
     */
    private LoginResponse construireLoginResponse(Utilisateur user, String jwtToken) {
        // Mapper les groupes
        List<LoginResponse.GroupeDTO> groupesDTOs = user.getGroupes() != null
                ? user.getGroupes()
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

        // Mapper les préférences
        LoginResponse.PreferencesDTO preferencesDTO = null;
        if (user.getPreferences() != null) {
            preferencesDTO = LoginResponse.PreferencesDTO.builder()
                    .id(user.getPreferences().getId())
                    .typeAlerte(user.getPreferences().getTypeAlerte() != null
                            ? user.getPreferences().getTypeAlerte().toString()
                            : "INSCRIPTION")
                    .canalEmail(user.getPreferences().isCanalEmail())
                    .canalInApp(user.getPreferences().isCanalInApp())
                    .actif(user.getPreferences().isActif())
                    .build();
        }

        // 🟢 Construire et retourner la réponse COMPLÈTE
        return LoginResponse.builder()
                .id(user.getId())
                .token(jwtToken)
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .matricule(user.getMatricule())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .departement(user.getDepartement())
                .statut(user.getStatut() != null
                        ? user.getStatut().toString()
                        : "Actif")
                .groupes(groupesDTOs) // 📌 LES GROUPES !
                .preferences(preferencesDTO)
                .dateDernierConnex(user.getDate_dernier_Connex())
                .dateCreationCompte(user.getDate_Creation_Compte())
                .build();
    }

    public boolean changerMotDePasse(String email, String nouveauMotDePasse) {
        Optional<Utilisateur> userOptional = utilisateurRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            Utilisateur user = userOptional.get();

            user.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
            user.setMotDepassetemporaire(false);
            utilisateurRepository.save(user);

            journalAuditService.enregistrerLog(ActionAudit.CHANGEMENT_MDP, "L'utilisateur a changé son mot de passe avec succès", user);

            return true;
        }

        return false;
    }

    public void demanderReinitialisationMotDePasse(String email) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();

            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiration(LocalDateTime.now().plusMinutes(15));
            utilisateurRepository.save(user);

            journalAuditService.enregistrerLog(ActionAudit.RESET_MDP, "Demande de réinitialisation de mot de passe initiée", user);

            emailService.evoymailrinitialisermotdepassse(email, token);
        } else {
            System.out.println("Tentative de réinitialisation pour un email inexistant : " + email);
            throw new RuntimeException("Adresse email introuvable ou non enregistrée.");
        }
    }

    public boolean reinitialiserMotDePasse(String token, String nouveauMotDePasse) {

        Optional<Utilisateur> userOpt = utilisateurRepository.findByResetToken(token);

        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();

            if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Le lien de réinitialisation a expiré.");
            }

            user.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
            user.setResetToken(null);
            user.setResetTokenExpiration(null);

            utilisateurRepository.save(user);

            journalAuditService.enregistrerLog(ActionAudit.CHANGEMENT_MDP, "Mot de passe réinitialisé via la procédure 'Mot de passe oublié'", user);

            return true;
        }

        throw new RuntimeException("Token de réinitialisation invalide.");
    }
}