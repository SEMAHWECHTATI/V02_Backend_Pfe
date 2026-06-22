package com.sagem.g2ii.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async; // 🛠️ Pour l'envoi en tâche de fond
import org.springframework.stereotype.Service;

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

    // 💡 Correction légère du nom de la méthode pour plus de propreté
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
}