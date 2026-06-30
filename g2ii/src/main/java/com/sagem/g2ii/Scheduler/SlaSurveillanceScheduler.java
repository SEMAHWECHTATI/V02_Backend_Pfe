package com.sagem.g2ii.Scheduler;

import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.ConfigurationGlobale;
import com.sagem.g2ii.Entity.Intervention.SLA;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.TicketRepo;
import com.sagem.g2ii.Service.ConfigurationGlobaleService; // 👈 AJOUTÉ
import com.sagem.g2ii.Service.EmailService;                // 👈 AJOUTÉ (votre service d'envoi)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 🚨 Service de surveillance des SLA en temps réel
 * Vérifie automatiquement toutes les 15 minutes si des tickets ont dépassé leurs délais SLA
 */
@Service
public class SlaSurveillanceScheduler {

    @Autowired
    private TicketRepo ticketRepository;

    @Autowired
    private ConfigurationGlobaleService configService; // 👈 AJOUTÉ (Option B)

    @Autowired
    private EmailService emailService; // 👈 AJOUTÉ (Pour notifier les retards)


    // ⏱️ Exécution automatique toutes les minutes
    @Scheduled( cron = "0 * * * * ?")
//           fixedRate = 90000  cron = "0 * * * * ?")
    @Transactional
    public void verifierDepassementSla() {
        System.out.println("📊 [SCHEDULER SLA] Démarrage de la vérification automatique...");

        // 1. Récupérer uniquement les tickets actifs (ex: statut 'Nouveau' ou 'En_Cours')
        List<Ticket> ticketsActifs = ticketRepository.findByStatutIn(List.of(StatutTicket.Nouveau, StatutTicket.En_Cours));
        System.out.println("   ⚙️ Total de tickets actifs à vérifier: " + ticketsActifs.size());
        int modifications = 0;

        for (Ticket ticket : ticketsActifs) {
            SLA sla = ticket.getSlaAssigne();

            // 🛑 Sécurité : Si le ticket n'a pas de SLA associé, on passe au suivant
            if (sla == null) {
                continue;
            }

            // 2. Calculer le temps écoulé depuis la création du ticket
            LocalDateTime maintenant = LocalDateTime.now();
            long minutesEcoulees = Duration.between(ticket.getDate(), maintenant).toMinutes();

            // 3. Convertir le délai du SLA (ex: 0.05h) en minutes
            double delaiPriseEnChargeMinutes = sla.getDelaiPriseEnChargeHeure() * 60;

            // 4. Vérifier si le ticket est en retard ET s'il n'a pas déjà été notifié
            // (Il faut ajouter un champ boolean 'slaPriseEnChargeDepasse' dans votre entité Ticket pour éviter les doublons)
            if (minutesEcoulees >= delaiPriseEnChargeMinutes &&
                    (ticket.getSlaPriseEnChargeDepasse() == null || !ticket.getSlaPriseEnChargeDepasse())) {

                System.out.println("🚨 [ALERTE] Le ticket " + ticket.getReference() + " a dépassé le délai de prise en charge !");
// 1. On vérifie si l'alerte a DEJA été envoyée (ou si le SLA est marqué dépassé)
                if ((ticket.getSlaPriseEnChargeDepasse() != null && ticket.getSlaPriseEnChargeDepasse())
                        || (ticket.getAlertePriseEnChargeEnvoyee() != null && ticket.getAlertePriseEnChargeEnvoyee())) {
                    continue; // Déjà traité, on passe au ticket suivant sans rien faire
                }

// 2. Envoi du mail (pensez à utiliser la version texte clair !)
                emailService.envoyerAlerteDepassementPriseEnCharge(ticket);

// 3. Sécurisation : ON MET À JOUR LES DEUX DRAPEAUX !
                ticket.setSlaPriseEnChargeDepasse(true);
                ticket.setAlertePriseEnChargeEnvoyee(true); // 👈 C'est cette ligne qui manquait !

// 4. On sauvegarde en base de données
                ticketRepository.save(ticket);
                modifications++;
            }
        }

        System.out.println("✅ [SCHEDULER SLA] Fin de la vérification. " + modifications + " ticket(s) modifié(s) et injecté(s).");
    }


    /**
     * S'exécute automatiquement toutes les 10 sec (10000 ms)
     * Vérifie les dépassements de SLA pour les tickets actifs (Nouveau ou En_Cours)
     */
    @Scheduled(fixedRate = 90000)
    @Transactional
    public void verifierDepassementsSlaEnTempsReel() {
        System.out.println("⏰ [SCHEDULER SLA] Tentative de vérification des dépassements SLA à " + LocalDateTime.now());

        // 🔐 INTERRUPTEUR GLOBAL : On récupère la configuration de l'admin
        ConfigurationGlobale config = configService.obtenirConfiguration();
        if (!config.isAlertesEmailActives()) {
            System.out.println("   🛑 [SCHEDULER SLA] Traitement annulé : L'administrateur a désactivé les alertes emails globales.");
            return;
        }

        LocalDateTime maintenant = LocalDateTime.now();

        // Récupérer uniquement les tickets actifs et non encore résolus/clôturés
        List<Ticket> ticketsActifs = ticketRepository.findByStatutIn(List.of(StatutTicket.Nouveau, StatutTicket.En_Cours));
        System.out.println("   📊 Total de tickets actifs à vérifier: " + ticketsActifs.size());

        int ticketsModifies = 0;

        for (Ticket ticket : ticketsActifs) {
            // ✅ VALIDATION : Ignorer les tickets sans SLA assigné
            if (ticket.getSlaAssigne() == null) {
                continue;
            }

            boolean aChange = false;
            boolean declencherAlertePriseEnCharge = false;
            boolean declencherAlerteResolution = false;

            // On calcule le temps écoulé en minutes pour une précision absolue (ex: 0.1h ou 0.2h)
            long minutesEcoulees = ChronoUnit.MINUTES.between(ticket.getDate(), maintenant);

            // =====================================================================
            // 🔍 CAS 1 : Vérifier SLA Prise en Charge (Délai ex: 0.1h -> 6 min)
            // =====================================================================
            if (ticket.getStatut() == StatutTicket.Nouveau && ticket.getSlaPriseEnChargeRespecte() == null) {
                double delaiMaxPriseEnChargeMinutes = ticket.getSlaAssigne().getDelaiPriseEnChargeHeure() * 60.0;

                if (minutesEcoulees >= delaiMaxPriseEnChargeMinutes) {
                    System.out.println("   🚨 [Ticket #" + ticket.getIdTicket() + "] SLA Prise en Charge DÉPASSÉ ! (" + minutesEcoulees + " min écoulées)");

                    ticket.setSlaPriseEnChargeRespecte(false);
                    aChange = true;
                    declencherAlertePriseEnCharge = true; // 🌟 FLAG ACTIVÉ POUR LE BLOC DU BAS
                }
            }

            // =====================================================================
            // 🔍 CAS 2 : Vérifier SLA Résolution (Délai ex: 0.2h -> 12 min)
            // =====================================================================
            if (ticket.getSlaResolutionRespecte() == null) {
                double delaiMaxResolutionMinutes = ticket.getSlaAssigne().getDelaiResolutionHeure() * 60.0;

                if (minutesEcoulees >= delaiMaxResolutionMinutes) {
                    System.out.println("   🚨 [Ticket #" + ticket.getIdTicket() + "] SLA Résolution DÉPASSÉ ! (" + minutesEcoulees + " min écoulées)");

                    ticket.setSlaResolutionRespecte(false);
                    ticket.setSlaRespecte(false); // Le SLA global est corrompu
                    aChange = true;
                    declencherAlerteResolution = true; // 🌟 FLAG ACTIVÉ POUR LE BLOC DU BAS
                }
            }

            // =====================================================================
            // 💾 SAUVEGARDE ET ENVOI D'ÉMAILS INSTANTANÉS (Temps réel)
            // =====================================================================
            if (aChange) {
                // 1. On sauvegarde d'abord en BDD pour verrouiller l'état
                ticketRepository.save(ticket);
                ticketsModifies++;
                System.out.println("   ✅ [Ticket #" + ticket.getIdTicket() + "] Statut SLA mis à jour en base de données");

                // 2. 🚀 Envoi immédiat en tâche de fond (@Async)
                if (declencherAlertePriseEnCharge) {
                    emailService.envoyerAlerteDepassementPriseEnCharge(ticket);
                    System.out.println("      ✉️ Email de retard de prise en charge envoyé au groupe : " +
                            (ticket.getGroupeAssigne() != null ? ticket.getGroupeAssigne().getNomGroupes() : "Aucun"));
                }

                if (declencherAlerteResolution) {
                    emailService.envoyerAlerteDepassementResolution(ticket);
                    System.out.println("      ✉️ Email de retard de résolution envoyé au technicien.");
                }
            }
        }

        System.out.println("✅ [SCHEDULER SLA] Fin de la vérification. " + ticketsModifies + " ticket(s) modifié(s).");
    }
}