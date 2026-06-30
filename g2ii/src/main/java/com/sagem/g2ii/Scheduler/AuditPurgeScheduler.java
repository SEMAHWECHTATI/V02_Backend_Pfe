package com.sagem.g2ii.Scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sagem.g2ii.Entity.Authentification.JournalAudit;
import com.sagem.g2ii.Repository.IntJournalAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuditPurgeScheduler {

    @Autowired
    private IntJournalAudit auditRepo;

// 🔥 Déclenchement automatique : Le 1er de chaque mois à minuit pile
    @Scheduled(cron = "0 0 0 1 * ?")
//           fixedRate = 10000 cron = "0 0 0 1 * ?"
    public void executerPurgeMensuelle() {
        // 1. Définir la limite de rétention (Ex: tout ce qui est plus vieux de 1 mois)
        LocalDateTime dateLimite = LocalDateTime.now().minusMonths(1);

        // 2. Récupérer les anciens logs correspondants
        List<JournalAudit> logsAPurger = auditRepo.findByDateActionBefore(dateLimite);

        if (logsAPurger.isEmpty()) {
            System.out.println("[AUDIT PURGE] Aucune donnée à purger pour le mois écoulé.");
            return;
        }

        // 📂 3. CONFIGURATION DE TON DOSSIER D:\Test pfe 2026\g2ii\logs
        String cheminDossierLogs = "D:/Test pfe 2026/g2ii/logs/logAuthentification";
        File dossierArchive = new File(cheminDossierLogs);

        // Si le dossier "logs" n'existe pas encore dans ton projet, Java va le créer tout seul
        if (!dossierArchive.exists()) {
            dossierArchive.mkdirs();
        }

        // Configuration du convertisseur JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Génération du fichier final (Ex: D:/Test pfe 2026/g2ii/logs/audit-archive-2026-05.txt)
        String cheminFichierFinal = cheminDossierLogs + "/audit-archive-" + dateLimite.getYear() + "-" + String.format("%02d", dateLimite.getMonthValue()) + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(cheminFichierFinal, true))) {

            // 4. Écriture de chaque ligne d'audit en JSON pur dans ton fichier
            for (JournalAudit log : logsAPurger) {
                String jsonLigne = mapper.writeValueAsString(log);
                writer.println(jsonLigne);
            }

            // 5. SÉCURITÉ : On vide la base de données après la réussite de l'écriture sur le disque D:
            auditRepo.deleteByDateActionBefore(dateLimite);

            System.out.println("[AUDIT PURGE] Succès ! " + logsAPurger.size() + " lignes archivées dans : " + cheminFichierFinal);

        } catch (Exception e) {
            // Si le disque D: a un problème, rien n'est supprimé de la base de données
            System.err.println("[AUDIT PURGE] ÉCHEC CRITIQUE sur le disque D: " + e.getMessage());
        }
    }}