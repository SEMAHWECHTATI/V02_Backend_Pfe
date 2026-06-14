package com.sagem.g2ii.Scheduler;

import com.sagem.g2ii.Service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinanceScheduler {

    private final ArticleService articleService;
    private final JdbcTemplate jdbcTemplate;

    // S'exécute automatiquement le dernier jour de chaque mois à 23h59:59
    @Scheduled(cron = "59 59 23 L * ?")
    public void enregistrerFermetureMensuelle() {
        try {
            BigDecimal valeurActuelle = articleService.getTotalInventoryValue();
            String anneeMois = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            String sql = "INSERT INTO historique_finance_stock (annee_mois, valeur_totale) VALUES (?, ?)";
            jdbcTemplate.update(sql, anneeMois, valeurActuelle);

            log.info("✅ Clôture financière mensuelle enregistrée pour {} : {} EUR", anneeMois, valeurActuelle);
        } catch (Exception e) {
            log.error("❌ Échec de la clôture financière mensuelle : {}", e.getMessage());
        }
    }
}