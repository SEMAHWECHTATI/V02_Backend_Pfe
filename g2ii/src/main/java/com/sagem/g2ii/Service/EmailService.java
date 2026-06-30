package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Email.EmailQueue;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.EmailQueueRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async; // 🛠️ Pour l'envoi en tâche de fond
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private EmailQueueRepository emailQueueRepository;

    // =========================================================================
    // 👥 SECTION 1 : GESTION DES COMPTES (Tes méthodes existantes conservées)
    // =========================================================================

    public void envoyerEmailBienvenue(String emailDestinataire, String prenom, String motDePasseTemporaire) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestinataire);
        message.setSubject("Bienvenue ! Votre compte a été approuvé 🎉");
        message.setText("Bonjour " + prenom + ",\n\n"
                + "Votre demande d'inscription a été acceptée avec succès.\n\n"
                + "Voici vos identifiants pour vous connecter à l'application :\n"
                + "-------------------------------------------------\n"
                + "👤 Login (Email) : " + emailDestinataire + "\n"
                + "🔑 Mot de passe  : " + motDePasseTemporaire + "\n"
                + "-------------------------------------------------\n\n"
                + "⚠️ Veuillez vous connecter et changer ce mot de passe immédiatement lors de votre première connexion.\n\n"
                + "Cordialement,\n"
                + "L'équipe Support IT");

        mailSender.send(message);
    }

    public void envoyerEmailRefus(String emailDestinataire, String prenom, String motif) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestinataire);
        message.setSubject("Mise à jour de votre demande d'inscription");
        message.setText("Bonjour " + prenom + ",\n\n"
                + "Nous vous informons que votre demande d'inscription n'a malheureusement pas pu être acceptée.\n\n"
                + "Motif du refus : " + motif + "\n\n"
                + "Si vous pensez qu'il s'agit d'une erreur, veuillez contacter le support.\n\n"
                + "Cordialement,\n"
                + "L'équipe Support IT");

        mailSender.send(message);
    }

    public void evoymailrinitialisermotdepassse(String emailDestinataire, String token) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestinataire);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour " + emailDestinataire + ",\n\n"
                + "Nous avons reçu une demande de réinitialisation de votre mot de passe.\n\n"
                + "LIEN DE RÉINITIALISATION : http://localhost/reset-password?token=" + token + "\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer ce message.\n\n"
                + "Cordialement,\n"
                + "L'équipe Support IT");

        mailSender.send(message);
    }

    // =========================================================================
    // 🚨 SECTION 2 : SYSTÈME D'ALERTES AUTOMATIQUES (Nouveaux ajouts Tâche 5)
    // =========================================================================

    /**
     * 📨 Envoi d'e-mails pour les alertes critiques de stocks ou violations de SLA
     * L'annotation @Async garantit que le thread principal (ou le Scheduler) n'est pas bloqué.
     */
    @Async
    public void envoyerEmailAlerteSysteme(String emailDestinataire, String objet, String contenuAlerte) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestinataire);
        message.setSubject("[GITI-KPI] 🚨 " + objet);

        message.setText("Bonjour,\n\n"
                + "Le système intelligent de surveillance GITI-KPI a détecté une anomalie nécessitant votre intervention immédiate.\n\n"
                + "Détails de l'événement :\n"
                + "-------------------------------------------------\n"
                + contenuAlerte + "\n"
                + "-------------------------------------------------\n\n"
                + "Veuillez vous rendre sur votre Tableau de bord pour traiter cette alerte.\n\n"
                + "Cordialement,\n"
                + "Moteur de Notification Automatisée Sagemcom");

        mailSender.send(message);
    }

    /**
     * Alerte pour le retard de Prise en Charge (Envoyé aux membres ou responsable du groupe)
     */
     // 👈 N'oubliez pas d'injecter votre Repository en haut de la classe !

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void envoyerAlerteDepassementPriseEnCharge(Ticket ticket) {
        if (ticket.getGroupeAssigne() == null) {
            System.out.println("⚠️ Impossible d'envoyer l'alerte : Aucun groupe assigné au ticket " + ticket.getReference());
            return;
        }

        // 1. Récupérer la liste des utilisateurs (techniciens) du groupe
        List<Utilisateur> techniciens = ticket.getGroupeAssigne().getUtilisateurs();

        if (techniciens == null || techniciens.isEmpty()) {
            System.out.println("⚠️ Aucun technicien trouvé dans le groupe " + ticket.getGroupeAssigne().getNomGroupes());
            return;
        }

        String sujet = "⚠️ RAPPEL SLA : Prise en charge dépassée - Ticket " + ticket.getReference();

        // 2. Parcourir tous les techniciens du groupe pour enregistrer le mail en texte brut
        for (Utilisateur technicien : techniciens) {
            if (technicien.getEmail() != null && !technicien.getEmail().trim().isEmpty()) {

                String nomComplet = (technicien.getPrenom() != null ? technicien.getPrenom() : "") + " " + (technicien.getNom() != null ? technicien.getNom() : "");
                if (nomComplet.trim().isEmpty()) nomComplet = technicien.getEmail();

                // 📝 CONSTRUCTION DU MESSAGE EN TEXTE CLAIR ET STRUCTURÉ
                String contenuTexte = "Bonjour " + nomComplet + ",\n\n"
                        + "Le ticket suivant n'a pas été pris en charge dans les délais impartis. Veuillez intervenir immédiatement.\n\n"
                        + "==================================================\n"
                        + "💥 DÉPASEMENT SLA - INFORMATIONS DU TICKET\n"
                        + "==================================================\n"
                        + "• Référence Unique   : " + ticket.getReference() + "\n"
                        + "• Titre du Ticket    : " + ticket.getTitre() + "\n"
                        + "• Groupe Responsable : " + ticket.getGroupeAssigne().getNomGroupes() + "\n"
                        + "• Niveau de Priorité : " + ticket.getPriorite() + "\n"
                        + "• Date de Création   : " + ticket.getDate() + "\n"
                        + "• Statut Actuel      : Nouveau (Non pris en charge)\n"
                        + "==================================================\n\n"
                        + "💡 Message généré automatiquement par G2II - Sagemcom";

                // 📝 Sauvegarde dans la table email_queue
                EmailQueue mailALivrer = new EmailQueue();
                mailALivrer.setDestinataire(technicien.getEmail());
                mailALivrer.setSujet(sujet);
                mailALivrer.setContenu(contenuTexte); // Contenu texte brut sans balises
                mailALivrer.setEnvoye(false);
                mailALivrer.setTentatives(0);

                emailQueueRepository.save(mailALivrer);

                System.out.println("💾 [EMAIL QUEUE] Alerte Plain-Text mise en attente pour : " + technicien.getEmail());
            }
        }
    }

    /**
     * Alerte pour le retard de Résolution (Texte clair)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void envoyerAlerteDepassementResolution(Ticket ticket) {
        String emailDestinataire = (ticket.getTechnicienAssigne() != null)
                ? ticket.getTechnicienAssigne().getEmail()
                : "manager-incident@sagem.com";

        String sujet = "🚨 ALERTE CRITIQUE SLA : Résolution hors délais - Ticket " + ticket.getReference();
        String nomTechnicien = (ticket.getTechnicienAssigne() != null) ? ticket.getTechnicienAssigne().getEmail() : "Aucun technicien assigné";
        Double delaiMax = (ticket.getSlaAssigne() != null) ? ticket.getSlaAssigne().getDelaiResolutionHeure() : 0.0;

        // 📝 CONSTRUCTION DU MESSAGE EN TEXTE CLAIR (VERSION ALERTE CRITIQUE)
        String contenuTexte = "ATTENTION,\n\n"
                + "Le contrat d'engagement de service (SLA) a expiré pour ce ticket. Une action immédiate de résolution est requise.\n\n"
                + "==================================================\n"
                + "🚨 EXCLUSION DU DÉLAI DE RÉSOLUTION\n"
                + "==================================================\n"
                + "• Référence Unique        : " + ticket.getReference() + "\n"
                + "• Titre du Ticket         : " + ticket.getTitre() + "\n"
                + "• Technicien Responsable : " + nomTechnicien + "\n"
                + "• Temps Max Alloué       : " + delaiMax + " Heures\n"
                + "• Statut Traitement      : HORS DÉLAIS ❌\n"
                + "==================================================\n\n"
                + "💡 Escalade de niveau 2 - Supervision Sagemcom G2II";

        envoyerEmailHtml(emailDestinataire, sujet, contenuTexte);
        // Note : Pensez à renommer votre méthode en 'envoyerEmailTexte' ou adaptez-la pour gérer du texte brut.
    }
    /**
     * Méthode utilitaire privée pour l'envoi technique de messages HTML
     */
    private void envoyerEmailHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = active le rendu HTML
            helper.setFrom("g2ii-system@sagem.com");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Impossible d'envoyer l'e-mail SLA à " + to + " : " + e.getMessage());
        }
    }
}