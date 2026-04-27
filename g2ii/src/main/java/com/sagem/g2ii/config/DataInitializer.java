package com.sagem.g2ii.Config;

import com.sagem.g2ii.Service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.init-data.categories", havingValue = "true") // 👈 J'ai ajouté les paramètres ici
public class DataInitializer {

    @Autowired
    private CategorieService categorieService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        System.out.println("\n════════════════════════════════════════════════════════════");
        System.out.println("🚀 INITIALISATION DES DONNÉES");
        System.out.println("════════════════════════════════════════════════════════════");

        try {
            categorieService.initialiserCategories();

            System.out.println("════════════════════════════════════════════════════════════");
            System.out.println("✅ INITIALISATION COMPLÉTÉE");
            System.out.println("════════════════════════════════════════════════════════════\n");

        } catch (Exception e) {
            System.err.println("\n❌ ERREUR LORS DE L'INITIALISATION:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        }
    }
}