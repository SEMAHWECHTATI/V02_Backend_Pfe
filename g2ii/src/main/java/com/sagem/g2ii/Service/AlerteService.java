package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.AlerteDTO;
import com.sagem.g2ii.Entity.Authentification.PreferenceNotification;
import com.sagem.g2ii.Entity.Authentification.Utilisateur; // 🛠️ Ajouté
import com.sagem.g2ii.Entity.Enumeration.*;
import com.sagem.g2ii.Entity.Inventaire.Alerte;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Notification;
import com.sagem.g2ii.Repository.AlerteRepository;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Repository.NotificationRepository;
import com.sagem.g2ii.Repository.PreferenceNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final PreferenceNotificationRepository preferenceRepository;
    private final IntUtilisateur utilisateurRepository; // 🛠️ Nouvelle injection pour le routage dynamique

    /**
     * ✅ Créer une alerte sécurisée ET initier son routage dynamique intelligent
     */
    public AlerteDTO creerAlerte(Article article, String message) {
        log.info("⚠️ Analyse d'anomalie de stock pour l'article: {}", article.getDesignation());

        TypeAlerte type;
        Severite severite;

        // Détermination des types et sévérités selon les stocks réels
        if (article.getQuantiteEnStock() == 0) {
            type = TypeAlerte.RUPTURE_STOCK;
            severite = Severite.CRITIQUE;
        } else if (article.getQuantiteEnStock() <= article.getSeuilCritique()) {
            type = TypeAlerte.SEUIL_CRITIQUE;
            severite = Severite.HAUTE;
        } else {
            type = TypeAlerte.SEUIL_MINIMUM;
            severite = Severite.MOYENNE;
        }

        Alerte alerte = Alerte.builder()
                .type(type)
                .message(message)
                .severite(severite)
                .statut(StatutAlerte.NOUVELLE)
                .build();

        alerte.addArticle(article);

        Alerte saved = alerteRepository.save(alerte);
        log.info("✅ Alerte stock persistée en BDD [ID: {}]. Calcul du routage des destinataires...", saved.getId());

        // 🚀 ROUTAGE DYNAMIQUE ET SÉCURISÉ DES DESTINATAIRES
        List<Utilisateur> destinatairesCibles = new ArrayList<>();

        if (severite == Severite.CRITIQUE) {
            // 🔴 CAS 1 : Escalade automatique à tous les Administrateurs
            log.info("🚨 Gravité CRITIQUE détectée : Extraction des profils Administrateurs.");
            destinatairesCibles = utilisateurRepository.findByRole(roleUtilisateur.Administrateur);
        } else {
            // 🟡 CAS 2 : Alerte standard (Sévérité HAUTE ou MOYENNE)
            if (article.getGroupe() != null) {
                Long groupeId = article.getGroupe().getId();
                log.info("👥 Gravité standard : Extraction du personnel affecté au groupe ID: {}", groupeId);

                // 🛠️ On récupère les Techniciens DU GROUPE
                List<Utilisateur> techniciens = utilisateurRepository.findByRoleAndGroupesId(roleUtilisateur.Technicien, groupeId);

                // 🛠️ On récupère les Gestionnaires de Stock DU MÊME GROUPE
                List<Utilisateur> gestionnaires = utilisateurRepository.findByRoleAndGroupesId(roleUtilisateur.Gestionnaire_Stock, groupeId);

                // On combine tout le monde dans notre liste de destinataires cibles
                destinatairesCibles.addAll(techniciens);
                destinatairesCibles.addAll(gestionnaires);

                log.info("📈 Destinataires trouvés pour le groupe {} : {} technicien(s) et {} gestionnaire(s).",
                        groupeId, techniciens.size(), gestionnaires.size());
            } else {
                // 🔒 Sécurité : L'article n'a pas de groupe, on ne fait aucun traitement sur le groupe pour éviter le crash
                log.warn("⚠️ L'article '{}' n'est associé à aucun groupe technique ou de gestion. Aucune notification de groupe envoyée.",
                        article.getDesignation());
            }
        }

        // 📨 Distribution individualisée basée sur les préférences uniques de chaque utilisateur trouvé
        String titreNotification = "[" + severite + "] Alerte de Stock : " + article.getDesignation();
        for (Utilisateur utilisateur : destinatairesCibles) {
            distribuerNotificationsSelonPreferences(
                    saved,
                    titreNotification,
                    utilisateur.getId(),
                    utilisateur.getEmail() // Extraction dynamique de l'email depuis la BDD
            );
        }

        return convertToDTO(saved);
    }

    /**
     * ✅ Distribue et audite les notifications en fonction des VRAIES préférences de l'utilisateur (In-App / Email)
     */
    private void distribuerNotificationsSelonPreferences(Alerte alerte, String titre, Long utilisateurId, String emailUtilisateur) {
        List<Notification> historiqueNotifications = new ArrayList<>();

        // 🔍 1. Charger la configuration de l'utilisateur depuis la table 'preferencenotification'
        PreferenceNotification prefs = preferenceRepository.findByUtilisateurId(utilisateurId)
                .orElse(null);

        // Si l'utilisateur n'a pas encore configuré ses interrupteurs, on active tout par défaut (Sécurité)
        boolean estActif = (prefs == null || prefs.isActif());
        boolean veutWeb = (prefs == null || prefs.isCanalInApp());
        boolean veutEmail = (prefs == null || prefs.isCanalEmail());

        if (!estActif) {
            log.info("🔕 Les notifications sont désactivées dans le profil de l'utilisateur ID: {}", utilisateurId);
            return;
        }

        // 📡 2. Canal : TABLEAU_BORD / IN-APP (Diffusion temps réel via WebSocket STOMP pour Angular)
        if (veutWeb) {
            Notification notifWeb = Notification.builder()
                    .alerte(alerte)
                    .titre(titre)
                    .contenu(alerte.getMessage())
                    .canal(CanalNotification.TABLEAU_BORD)
                    .statut(StatutNotification.ENVOYEE)
                    .dateEnvoi(LocalDateTime.now())
                    .build();
            historiqueNotifications.add(notifWeb);

            try {
                messagingTemplate.convertAndSend("/topic/inventory-alertes", convertToDTO(alerte));
            } catch (Exception e) {
                log.error("❌ Échec de la transmission sur le canal WebSocket : {}", e.getMessage());
            }
        }

        // 📧 3. Canal : EMAIL (Prise en charge asynchrone sécurisée par ta file d'attente EmailQueue)
        if (veutEmail && emailUtilisateur != null && !emailUtilisateur.isEmpty()) {
            // Filtrage : Envoi d'e-mail uniquement pour les urgences opérationnelles (Haute ou Critique)
            if (alerte.getSeverite() == Severite.CRITIQUE || alerte.getSeverite() == Severite.HAUTE) {

                Notification notifEmail = Notification.builder()
                        .alerte(alerte)
                        .titre(titre)
                        .contenu(alerte.getMessage())
                        .canal(CanalNotification.EMAIL)
                        .statut(StatutNotification.EN_ATTENTE)
                        .build();

                try {
                    // Délégation sécurisée à ton EmailService
                    emailService.envoyerEmailAlerteSysteme(emailUtilisateur, titre, alerte.getMessage());
                    notifEmail.setStatut(StatutNotification.ENVOYEE);
                    notifEmail.setDateEnvoi(LocalDateTime.now());
                } catch (Exception e) {
                    log.error("❌ Échec de traitement postal pour l'adresse {} : {}", emailUtilisateur, e.getMessage());
                    notifEmail.setStatut(StatutNotification.ECHOUEE);
                }
                historiqueNotifications.add(notifEmail);
            }
        }

        // 📊 4. Sauvegarde de l'historique complet pour la traçabilité d'audit (PostgreSQL)
        if (!historiqueNotifications.isEmpty()) {
            notificationRepository.saveAll(historiqueNotifications);
        }
    }

    // --- Méthodes d'administration et d'exposition (Totalement préservées) ---

    public void marquerCommeLue(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
        alerte.setStatut(StatutAlerte.LUE);
        alerteRepository.save(alerte);
        log.info("👁️ Alerte marquée comme lue: {}", alerteId);
    }

    public void marquerCommeTraitee(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
        alerte.setStatut(StatutAlerte.TRAITEE);
        alerteRepository.save(alerte);
        log.info("✅ Alerte marquée comme traitée: {}", alerteId);
    }

    public List<AlerteDTO> getAlerteNonTraitees() {
        return alerteRepository.findByStatut(StatutAlerte.NOUVELLE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AlerteDTO> getAlerteCritique() {
        return alerteRepository.findByStatutAndSeveriteOrderByDateCreationDesc(
                        StatutAlerte.NOUVELLE, Severite.CRITIQUE
                ).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // À ajouter dans AlerteService.java (par exemple au-dessus de convertToDTO)
    public AlerteDTO getAlerteById(Long id) {
        Alerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerte introuvable avec l'ID : " + id));
        return convertToDTO(alerte);
    }

    /**
     * 🔄 Convertir l'entité Alerte en DTO sécurisé pour la couche d'exposition Angular
     */
    private AlerteDTO convertToDTO(Alerte alerte) {
        List<Article> articlesAssocies = alerte.getArticles() != null ? alerte.getArticles() : new ArrayList<>();

        return AlerteDTO.builder()
                .id(alerte.getId())
                .type(alerte.getType())
                .message(alerte.getMessage())
                .severite(alerte.getSeverite())
                .statut(alerte.getStatut())
                .dateCreation(alerte.getDateCreation())
                .dateAcquittement(alerte.getDateAcquittement())
                .articleIds(articlesAssocies.stream().map(Article::getId).collect(Collectors.toList()))
                .articleDesignations(articlesAssocies.stream().map(Article::getDesignation).collect(Collectors.toList()))
                .build();
    }

    public void supprimerAlerte(Long alerteId) {
        log.info("🗑️ Suppression alerte: {}", alerteId);
        alerteRepository.deleteById(alerteId);
    }
}