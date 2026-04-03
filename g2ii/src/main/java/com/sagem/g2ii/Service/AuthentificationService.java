package com.sagem.g2ii.Service;


import com.sagem.g2ii.DTOs.LoginRequest;
import com.sagem.g2ii.DTOs.LoginResponse;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.statutUtilisateur;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.securiter.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthentificationService {
    private final IntUtilisateur utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Remplacez LoginRequest et LoginResponse par vos propres classes DTO
    public LoginResponse authentifier(LoginRequest request) {

        // 1. Chercher l'utilisateur par son email
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 2. Vérifier si le compte est déjà bloqué
        if (user.getStatut() == statutUtilisateur.Bloque) {
            throw new RuntimeException("Votre compte est bloqué suite à trop de tentatives infructueuses. Veuillez contacter l'administrateur.");
        }

        // 3. Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getMotDePasse(), user.getMotDePasse())) {

            // ❌ ÉCHEC : Mauvais mot de passe ! On incrémente le compteur
            int tentatives = user.getTentative_login() + 1;
            user.setTentative_login(tentatives);

            // Si on atteint 3 tentatives, on bloque le compte
            if (tentatives >= 3) {
                user.setStatut(statutUtilisateur.Bloque);
                utilisateurRepository.save(user); // On sauvegarde en base
                throw new RuntimeException("Compte bloqué : 3 tentatives infructueuses.");
            }

            utilisateurRepository.save(user); // On sauvegarde la nouvelle tentative
            throw new RuntimeException("Mot de passe incorrect (" + tentatives + "/3)");
        }

        // 4. ✅ SUCCÈS : Le mot de passe est bon !
        user.setTentative_login(0); // On remet le compteur à zéro
        user.setDate_dernier_Connex(LocalDateTime.now()); // On met à jour la date de connexion
        utilisateurRepository.save(user); // On sauvegarde en base

        // 5. Générer le fameux Token JWT
        String jwtToken = jwtService.generateToken(user);

        // 6. Renvoyer le Token au Frontend (Angular)
        return LoginResponse.builder().token(jwtToken).build();    }

    // N'oubliez pas l'import s'il manque : import java.util.Optional;

    public boolean changerMotDePasse(String email, String nouveauMotDePasse) {
        // 1. On cherche l'utilisateur dans la base de données
        Optional<Utilisateur> userOptional = utilisateurRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            Utilisateur user = userOptional.get();

            // 2. On crypte le nouveau mot de passe avant de le sauvegarder (TRÈS IMPORTANT)
            user.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));

            // 3. Puisque l'utilisateur vient de changer son mot de passe,
            // on s'assure que le système sait que ce n'est plus un mot de passe temporaire
            user.setMotDepassetemporaire(false);

            // 4. On sauvegarde les modifications dans la base de données
            utilisateurRepository.save(user);

            return true; // Le changement a réussi
        }

        return false; // L'utilisateur n'existe pas
    }
}
