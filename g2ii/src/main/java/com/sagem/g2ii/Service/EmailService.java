package com.sagem.g2ii.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

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

        // Pas de try/catch ici ! Si pas d'internet, l'erreur remontera au DemandeService.
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
                + "LIEN DE RÉINITIALISATION : http://localhost:4200/reset-password?token=" + token + "\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer ce message.\n\n"
                + "Cordialement,\n"
                + "L'équipe Support IT");

        mailSender.send(message);
    }
}