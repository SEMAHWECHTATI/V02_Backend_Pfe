package com.sagem.g2ii.Scheduler;


import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.TicketRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 🚨 Service de surveillance des SLA en temps réel
 * Vérifie automatiquement tous les 15 minutes si des tickets ont dépassé leurs délais SLA
 */
@Service
public class SlaSurveillanceScheduler {

    @Autowired
    private TicketRepo ticketRepository;


    /**
     * S'exécute automatiquement toutes les 15 minutes (900000 ms)
     * Vérifie les dépassements de SLA pour les tickets actifs (Nouveau ou En_Cours)
     */
    @Scheduled(fixedRate = 900000)
    @Transactional
    public void verifierDepassementsSlaEnTempsReel() {
        System.out.println("⏰ [SCHEDULER SLA] Vérification des dépassements SLA à " + LocalDateTime.now());

        LocalDateTime maintenant = LocalDateTime.now();

        // Récupérer uniquement les tickets actifs et non encore résolus/clôturés
        List<Ticket> ticketsActifs = ticketRepository.findByStatutIn(List.of(StatutTicket.Nouveau, StatutTicket.En_Cours));

        System.out.println("   📊 Total de tickets actifs à vérifier: " + ticketsActifs.size());

        int ticketsModifies = 0;

        for (Ticket ticket : ticketsActifs) {
            // ✅ VALIDATION: Ignorer les tickets sans SLA assigné
            if (ticket.getSlaAssigne() == null) {
                continue;
            }

            boolean aChange = false;

            // ============================================
            // 🔍 CAS 1: Vérifier SLA Prise en Charge
            // ============================================
            // Le ticket est toujours "Nouveau" et dépasse le délai max de prise en charge
            if (ticket.getStatut() == StatutTicket.Nouveau && ticket.getSlaPriseEnChargeRespecte() == null) {
                long heuresDepuisCreation = ChronoUnit.HOURS.between(ticket.getDate(), maintenant);
                int delaiMaxPriseEnCharge = ticket.getSlaAssigne().getDelaiPriseEnChargeHeure();

                // ✅ CORRECTION: Utiliser >= au lieu de > pour stricte conformité
                if (heuresDepuisCreation >= delaiMaxPriseEnCharge) {
                    System.out.println("   🚨 [Ticket #" + ticket.getIdTicket() + "] SLA Prise en Charge DÉPASSÉ!");
                    System.out.println("      Créé: " + ticket.getDate());
                    System.out.println("      Heures écoulées: " + heuresDepuisCreation + "h");
                    System.out.println("      Délai max autorisé: " + delaiMaxPriseEnCharge + "h");

                    ticket.setSlaPriseEnChargeRespecte(false);
                    ticket.setSlaRespecte(false);
                    aChange = true;
                }
            }

            // ============================================
            // 🔍 CAS 2: Vérifier SLA Résolution
            // ============================================
            // Le ticket (Nouveau ou En Cours) a globalement dépassé le temps de résolution total autorisé
            if (ticket.getSlaResolutionRespecte() == null) {
                long heuresDepuisCreation = ChronoUnit.HOURS.between(ticket.getDate(), maintenant);
                int delaiMaxResolution = ticket.getSlaAssigne().getDelaiResolutionHeure();

                // ✅ CORRECTION: Utiliser >= pour stricte conformité
                if (heuresDepuisCreation >= delaiMaxResolution) {
                    System.out.println("   🚨 [Ticket #" + ticket.getIdTicket() + "] SLA Résolution DÉPASSÉ!");
                    System.out.println("      Créé: " + ticket.getDate());
                    System.out.println("      Heures écoulées: " + heuresDepuisCreation + "h");
                    System.out.println("      Délai max autorisé: " + delaiMaxResolution + "h");

                    ticket.setSlaResolutionRespecte(false);
                    ticket.setSlaRespecte(false);
                    aChange = true;
                }
            }

            // ============================================
            // 💾 Sauvegarde en base si le SLA a expiré
            // ============================================
            if (aChange) {
                ticketRepository.save(ticket);
                ticketsModifies++;
                System.out.println("   ✅ [Ticket #" + ticket.getIdTicket() + "] Sauvegardé en base de données");
            }
        }

        System.out.println("✅ [SCHEDULER SLA] Fin de la vérification. " + ticketsModifies + " ticket(s) modifié(s).");
    }
}
