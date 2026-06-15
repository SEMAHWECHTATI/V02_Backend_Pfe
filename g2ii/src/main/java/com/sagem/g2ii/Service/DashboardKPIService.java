package com.sagem.g2ii.Service;


import com.sagem.g2ii.DTOs.KPIInterventionDTO;
import com.sagem.g2ii.DTOs.KPIInventoryDTO;
import com.sagem.g2ii.DTOs.KPIPerformanceDTO;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.TicketRepo;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardKPIService {

    private final TicketRepo ticketRepository;
    private final ArticleRepository articleRepository;

    public KPIInterventionDTO getInterventionKPIs() {
        KPIInterventionDTO dto = new KPIInterventionDTO();

        long total = ticketRepository.count();
        dto.setTotalInterventions(total);

        Double avgTime = ticketRepository.getAverageResolutionTimeInHours();
        dto.setTempsMoyenResolution(avgTime != null ? avgTime : 0.0);

        // Tendances Temporelles
        Map<String, Long> periodes = new HashMap<>();
        periodes.put("jour", ticketRepository.countTicketsDepuis(LocalDateTime.now().minusDays(1)));
        periodes.put("semaine", ticketRepository.countTicketsDepuis(LocalDateTime.now().minusWeeks(1)));
        periodes.put("mois", ticketRepository.countTicketsDepuis(LocalDateTime.now().minusMonths(1)));
        dto.setInterventionsParPeriode(periodes);

        long clotures = ticketRepository.countCloturedTickets();
        dto.setTauxCloture(total > 0 ? ((double) clotures / total) * 100 : 0.0);
        dto.setInterventionsEnRetardSLA(0L);

        // Répartitions
        dto.setRepartitionParDomaine(ticketRepository.countByCategorie().stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1])));

        dto.setRepartitionParDemandeur(ticketRepository.countByDemandeur().stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1])));

        // ✅ Temps moyen par domaine & technicien
        dto.setTempsMoyenParDomaine(ticketRepository.getAverageResolutionTimeByDomaine().stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Double) r[1])));

        dto.setTempsMoyenParTechnicien(ticketRepository.getAverageResolutionTimeByTechnicien().stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Double) r[1])));

        return dto;
    }

    public KPIPerformanceDTO getPerformanceKPIs() {
        KPIPerformanceDTO dto = new KPIPerformanceDTO();

        // 1. Ton code existant pour les techniciens
        dto.setInterventionsParTechnicien(ticketRepository.countByTechnicienAssigne().stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1])));

        // 2. ✅ CORRECTION : Extraction dynamique du domaine le plus demandé
        // On va chercher dans les tickets la catégorie qui a le plus grand nombre d'occurrences
        String domaineTop = ticketRepository.countByCategorie().stream()
                .findFirst() // Le premier élément de ta liste triée par COUNT DESC
                .map(r -> (String) r[0])
                .orElse("Aucun");

        dto.setDomaineLePlusDemande(domaineTop); // Remplacera le 'null' par "Demande D'intervention Réseaux IT"

        // 3. Tes valeurs de simulation existantes
        dto.setScoreSatisfactionMoyen(4.5);
        dto.setCoutMoyenIntervention(120.0);

        return dto;
    }

    public KPIInventoryDTO getInventoryKPIs() {
        KPIInventoryDTO dto = new KPIInventoryDTO();
        Double totalValue = articleRepository.calculateTotalAssetValue();
        dto.setValeurTotalePatrimoineIT(totalValue != null ? totalValue : 0.0);
        dto.setArticlesEnRupture(articleRepository.countOutOfStockArticles());

        Double avgAvailability = articleRepository.getAverageAvailabilityRate();
        dto.setTauxDisponibilite(avgAvailability != null ? avgAvailability : 100.0);

        // Logique personnalisée pour la rotation du stock et l'utilisation
        dto.setRotationStock(4.2); // Exemple de valeur fixe ou calculée via MouvementStock
        dto.setTauxUtilisationMateriel(85.5);

        return dto;
    }


}