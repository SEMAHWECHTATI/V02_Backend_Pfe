package com.sagem.g2ii.Config;

import com.sagem.g2ii.Service.CategorieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "app.init-data.categories", havingValue = "true")
@RequiredArgsConstructor
@Slf4j // 🌟 Utilisation du logger standard à la place de System.out
public class DataInitializer {

    private final CategorieService categorieService;

    // 🎯 SÉCURITÉ ANTI-DOUBLON : Garantit une exécution unique au sein du cycle de vie de la JVM
    private final AtomicBoolean isAlreadyInitialized = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        // Si l'initialisation a déjà été déclenchée dans cette instance, on passe notre tour
        if (isAlreadyInitialized.getAndSet(true)) {
            log.warn("⚠️ [DATA INIT] ApplicationReadyEvent reçu à nouveau, exécution ignorée pour éviter les doublons.");
            return;
        }

        log.info("════════════════════════════════════════════════════════════");
        log.info("🚀 [DATA INIT] DÉMARRAGE DE L'INITIALISATION DES DONNÉES SYSTEME");
        log.info("════════════════════════════════════════════════════════════");

        try {
            categorieService.initialiserCategories();

            log.info("════════════════════════════════════════════════════════════");
            log.info("✅ [DATA INIT] INITIALISATION TERMINÉE AVEC SUCCÈS");
            log.info("════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("❌ [DATA INIT] ERREUR FATALE LORS DE L'INITIALISATION : {}", e.getMessage(), e);
            // Optionnel : Remettre à false si tu veux permettre une tentative ultérieure en cas d'échec
            isAlreadyInitialized.set(false);
        }
    }
}