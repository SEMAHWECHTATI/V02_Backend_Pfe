package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Intervention.ConfigurationGlobale;
import com.sagem.g2ii.Repository.ConfigurationGlobaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // 🌟 Injection propre par constructeur
@Slf4j // 🌟 Logging standardisé via Slf4j
public class ConfigurationGlobaleService {

    private final ConfigurationGlobaleRepository repository;
    private final JournalAuditService journalAuditService; // 🌟 Injection du service d'audit

    /**
     * Helper pour récupérer l'opérateur actuellement connecté via Spring Security
     */
    private Utilisateur getConnectedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Utilisateur) {
                return (Utilisateur) principal;
            }
        } catch (Exception e) {
            // Context vide (Démarrage, Thread asynchrone, etc.)
        }
        return null;
    }

    /**
     * Récupérer la config (ou en créer une par défaut si la table est vide)
     */
    @Transactional
    public ConfigurationGlobale obtenirConfiguration() {
        return repository.findById(1L).orElseGet(() -> {
            log.info("⚙️ [CONFIG] Table vide. Création de la configuration globale par défaut.");

            ConfigurationGlobale defaut = ConfigurationGlobale.builder()
                    .id(1L)
                    .indiceFaisabiliteEquipe(10.0)
                    .alertesEmailActives(true)
                    .autoAssignationActive(true)
                    .build();

            ConfigurationGlobale saved = repository.save(defaut);

            // 🌟 LOG D'AUDIT SYSTEME (Création de la configuration initiale)
            journalAuditService.enregistrerLogAvance(
                    ModuleAudit.GESTION_APPLICATION, // 🎯 Utilisation de ton module cible
                    ActionAudit.APPROBATION_DEMANDE,
                    "ConfigurationGlobale",
                    saved.getId(),
                    "Génération automatique des paramètres système par défaut.",
                    null,
                    "{indiceFaisabilite:10.0, alertesEmail:true, autoAssignation:true}",
                    NiveauAudit.INFO,
                    true,
                    null // Toujours null au boot de l'application
            );

            return saved;
        });
    }

    /**
     * Mettre à jour la config application
     */
    @Transactional
    public ConfigurationGlobale sauvegarderConfiguration(ConfigurationGlobale nouvelleConfig) {
        log.info("⚙️ [CONFIG] Mise à jour des paramètres de l'application par un administrateur.");

        // 🌟 1. On récupère l'état actuel en base avant d'écraser, pour l'historique d'audit
        ConfigurationGlobale ancienneConfig = repository.findById(1L).orElse(null);

        String ancienneValeurStr = "Inconnue";
        if (ancienneConfig != null) {
            ancienneValeurStr = String.format("{indiceFaisabilite: %s, alertesEmail: %b, autoAssignation: %b}",
                    ancienneConfig.getIndiceFaisabiliteEquipe(),
                    ancienneConfig.isAlertesEmailActives(),
                    ancienneConfig.isAutoAssignationActive());
        }

        // 2. On force l'ID unique et on sauvegarde
        nouvelleConfig.setId(1L);
        ConfigurationGlobale saved = repository.save(nouvelleConfig);

        // Formater la nouvelle chaîne pour l'audit log
        String nouvelleValeurStr = String.format("{indiceFaisabilite: %s, alertesEmail: %b, autoAssignation: %b}",
                saved.getIndiceFaisabiliteEquipe(),
                saved.isAlertesEmailActives(),
                saved.isAutoAssignationActive());

        // 🌟 3. LOG D'AUDIT : Changement des paramètres de l'application
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.GESTION_APPLICATION, // 🎯 Module cible pour les paramètres
                ActionAudit.CHANGEMENT_MDP,       // Utilisé ici au sens générique de modification / mise à jour
                "ConfigurationGlobale",
                saved.getId(),
                "Modification manuelle des paramètres de configuration globale de l'application.",
                ancienneValeurStr,
                nouvelleValeurStr,
                NiveauAudit.WARNING, // Changement système = Niveau d'attention Warning
                true,
                getConnectedUser() // Capture l'administrateur à l'origine du changement
        );

        return saved;
    }
}