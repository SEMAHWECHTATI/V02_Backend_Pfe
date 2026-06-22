package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async; // 🛠️ Pour l'envoi en tâche de fond
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

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

        String contenuHtml = "⚠️ <b>Alerte Dépassement SLA</b><br/><br/>"
                + "Le ticket suivant n'a pas été pris en charge dans les délais impartis par votre groupe (<b>"
                + ticket.getGroupeAssigne().getNomGroupes() + "</b>) :<br/>"
                + "• <b>Référence :</b> " + ticket.getReference() + "<br/>"
                + "• <b>Titre :</b> " + ticket.getTitre() + "<br/>"
                + "• <b>Priorité :</b> " + ticket.getPriorite() + "<br/>"
                + "• <b>Date de création :</b> " + ticket.getDate() + "<br/><br/>"
                + "<i>Veuillez prendre en charge ce ticket de toute urgence.</i>";

        // 2. Parcourir tous les techniciens du groupe pour leur envoyer l'e-mail individuellement
        for (Utilisateur technicien : techniciens) {
            // Si votre projet accepte le isEmpty(), cette ligne est parfaitement valide :
            if (technicien.getEmail() != null && !technicien.getEmail().trim().isEmpty()) {
                envoyerEmailHtml(technicien.getEmail(), sujet, contenuHtml);
                System.out.println("   ✉️ Email de retard envoyé au technicien : " + technicien.getEmail());
            }
        }
    }

    /**
     * Alerte pour le retard de Résolution (Envoyé au technicien assigné ou au manager)
     */
    public void envoyerAlerteDepassementResolution(Ticket ticket) {
        String emailDestinataire = (ticket.getTechnicienAssigne() != null)
                ? ticket.getTechnicienAssigne().getEmail()
                : "manager-incident@sagem.com";

        String sujet = "🚨 ALERTE CRITIQUE SLA : Résolution hors délais - Ticket " + ticket.getReference();

        String contenuHtml = "🚨 <b>Dépassement du Temps de Résolution</b><br/><br/>"
                + "Le délai de résolution maximal prévu par le contrat SLA a expiré :<br/>"
                + "• <b>Référence :</b> " + ticket.getReference() + "<br/>"
                + "• <b>Titre :</b> " + ticket.getTitre() + "<br/>"
                + "• <b>Technicien en charge :</b> " + (ticket.getTechnicienAssigne() != null ? ticket.getTechnicienAssigne().getEmail() : "Aucun") + "<br/>"
                + "• <b>Temps max alloué :</b> " + ticket.getSlaAssigne().getDelaiResolutionHeure() + " heures<br/><br/>"
                + "<i>Merci de procéder immédiatement à la résolution ou de documenter le retard.</i>";

        envoyerEmailHtml(emailDestinataire, sujet, contenuHtml);
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