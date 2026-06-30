package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.JournalAudit;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Repository.IntJournalAudit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalAuditService {

    private final IntJournalAudit journalAuditRepository;
    private final HttpServletRequest request;

    /**
     * Méthode 1 : Simplifiée (Garde la compatibilité avec ton AuthentificationService actuel)
     */
    public void enregistrerLog(ActionAudit action, String description, Utilisateur utilisateur) {
        // Déduction automatique du niveau et du succès
        NiveauAudit niveau = (action == ActionAudit.BLOCAGE || action == ActionAudit.ECHEC_CONNEXION)
                ? NiveauAudit.WARNING : NiveauAudit.INFO;

        Boolean succes = !(action == ActionAudit.ECHEC_CONNEXION);

        enregistrerLogAvance(
                ModuleAudit.AUTHENTIFICATION,
                action,
                "Utilisateur",
                utilisateur != null ? utilisateur.getId() : null,
                description,
                null,
                null,
                niveau,
                succes,
                utilisateur
        );
    }
    /**
     * Méthode 2 : Complète / Avancée (Pour les tickets, stocks, demandes, etc.)
     */
    public void enregistrerLogAvance(
            ModuleAudit module, ActionAudit action, String entite, Long entiteId,
            String description, String ancienneValeur, String nouvelleValeur,
            NiveauAudit niveau, Boolean succes, Utilisateur utilisateur
    ) {
        String ipAddress = "127.0.0.1";
        String userAgent = "Système / Init Application";

        // 🌟 SÉCURISATION : On tente de lire la requête HTTP uniquement si elle existe
        try {
            if (request != null && org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null) {
                String forwardedFor = request.getHeader("X-Forwarded-For");
                if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
                    ipAddress = forwardedFor.split(",")[0].trim();
                } else {
                    ipAddress = request.getRemoteAddr();
                }
                if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                    ipAddress = "127.0.0.1";
                }

                String headerAgent = request.getHeader("User-Agent");
                if (headerAgent != null) {
                    userAgent = headerAgent;
                }
            }
        } catch (Exception e) {
            // En cas d'appel hors contexte Web (ex: démarrage de l'application)
            log.debug("Appel du log d'audit en dehors d'un contexte de requête HTTP.");
        }

        String email = (utilisateur != null) ? utilisateur.getEmail() : "Anonyme/Inconnu";
        log.info("[AUDIT] {} | Action: {} | Par: {} | IP: {}", module, action, email, ipAddress);

        JournalAudit journal = JournalAudit.builder()
                .module(module)
                .action(action)
                .entite(entite != null ? entite : "Système")
                .entiteId(entiteId)
                .description(description)
                .ancienneValeur(ancienneValeur)
                .nouvelleValeur(nouvelleValeur)
                .adresseIp(ipAddress)
                .userAgent(userAgent)
                .niveau(niveau != null ? niveau : NiveauAudit.INFO)
                .succes(succes != null ? succes : true)
                .dateAction(LocalDateTime.now())
                .utilisateur(utilisateur)
                .build();

        journalAuditRepository.save(journal);
    }
}