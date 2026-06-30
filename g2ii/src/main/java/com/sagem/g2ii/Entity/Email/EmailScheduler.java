package com.sagem.g2ii.Entity.Email;

import com.sagem.g2ii.Repository.EmailQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailScheduler {

    @Autowired
    private EmailQueueRepository emailQueueRepository;

    @Autowired
    private JavaMailSender mailSender;

    // ⏱️ S'exécute toutes les 20 secondes (20000 ms) pour une réactivité maximale
    @Scheduled(fixedDelay = 20000)
    public void renvoyerEmailsEchoues() {
        // On récupère les emails non envoyés qui ont moins de 5 tentatives
        List<EmailQueue> fileDattente = emailQueueRepository.findByEnvoyeFalseAndTentativesLessThan(5);

        for (EmailQueue email : fileDattente) {
            try {
                // Tentative d'envoi
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email.getDestinataire());
                message.setSubject(email.getSujet());
                message.setText(email.getContenu());

                mailSender.send(message);

                // Si ça réussit :
                email.setEnvoye(true);
                emailQueueRepository.save(email);
                System.out.println("Email envoyé avec succès à " + email.getDestinataire());

            } catch (Exception e) {
                // Si ça échoue (pas d'internet), on incrémente les tentatives
                email.setTentatives(email.getTentatives() + 1);
                email.setDernierErreur(e.getMessage());
                emailQueueRepository.save(email);
                System.err.println("Échec envoi. Tentative n°" + email.getTentatives());
            }
        }
    }
}
