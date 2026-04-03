package com.sagem.g2ii.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void envoyerEmailBienvenue(String emailDestinataire, String prenom, String motDePasseTemporaire) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sameh.ouechtati@gmail.com");
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
            System.out.println("✅ Email envoyé avec succès à " + emailDestinataire);

        } catch (Exception e) {
            System.err.println("⚠️ Impossible d'envoyer l'email à " + emailDestinataire + ".");
            System.err.println("Détail de l'erreur : " + e.getMessage());
            System.err.println("👉 Mot de passe temporaire généré : " + motDePasseTemporaire);
        }
    }

    public void envoyerEmailRefus(String emailDestinataire, String prenom, String motif) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sameh20950@gmail.com");
            message.setTo(emailDestinataire);
            message.setSubject("Mise à jour de votre demande d'inscription");

            message.setText("Bonjour " + prenom + ",\n\n"
                    + "Nous vous informons que votre demande d'inscription n'a malheureusement pas pu être acceptée.\n\n"
                    + "Motif du refus : " + motif + "\n\n"
                    + "Si vous pensez qu'il s'agit d'une erreur, veuillez contacter le support.\n\n"
                    + "Cordialement,\n"
                    + "L'équipe Support IT");

            mailSender.send(message);
            System.out.println("✅ Email de refus envoyé avec succès à " + emailDestinataire);

        } catch (Exception e) {
            System.err.println("⚠️ Impossible d'envoyer l'email de refus à " + emailDestinataire + ".");
            System.err.println("Détail de l'erreur : " + e.getMessage());
        }
    }
}