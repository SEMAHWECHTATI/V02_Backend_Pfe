package com.sagem.g2ii.Scheduler;

import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.ConfigurationGlobale;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.TicketRepo;
import com.sagem.g2ii.Service.ConfigurationGlobaleService; // 👈 AJOUTÉ
import com.sagem.g2ii.Service.EmailService;                // 👈 AJOUTÉ (votre service d'envoi)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * S'exécute automatiquement toutes les 10 sec (10000 ms)
     * Vérifie les dépassements de SLA pour les tickets actifs (Nouveau ou En_Cours)
     */
    @Scheduled(fixedRate = 10000)
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