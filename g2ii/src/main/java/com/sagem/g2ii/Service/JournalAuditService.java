package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.JournalAudit;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Repository.IntJournalAudit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j // Génère les logs dans le fichier texte configuré dans application.yml
public class JournalAuditService {

    private final IntJournalAudit journalAuditRepository;

    // Spring Boot injecte automatiquement la requête HTTP en cours !
    private final HttpServletRequest request;

    public void enregistrerLog(ActionAudit action, String description, Utilisateur utilisateur) {

        // 1. Récupération intelligente de l'adresse IP
        // On vérifie d'abord l'en-tête "X-Forwarded-For" au cas où l'app est derrière un proxy ou load balancer
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr(); // Sinon, on prend l'IP directe
        }

        // 2. Écriture dans le fichier de log texte (console + fichier .log)
        String email = (utilisateur != null) ? utilisateur.getEmail() : "Utilisateur non trouvé";
        log.info("AUDIT SÉCURITÉ - Action: {} | IP: {} | User: {} | Détails: {}",
                action.name(), ipAddress, email, description);

        // 3. Création et sauvegarde dans la base de données
        JournalAudit journal = JournalAudit.builder()
                .action(action)
                .description(description)
                .adresseIp(ipAddress)
                .dateAction(LocalDateTime.now())
                .utilisateur(utilisateur)
                .build();

        journalAuditRepository.save(journal);
    }
}