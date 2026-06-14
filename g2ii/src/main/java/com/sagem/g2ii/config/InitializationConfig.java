package com.sagem.g2ii.Config;

import com.sagem.g2ii.Service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitializationConfig {

    @Autowired
    private CategorieService categorieService;

    /**
     * ✅ Initialiser les catégories et SLA au démarrage de l'application
     */
    @Bean
    public ApplicationRunner initializeCategories() {
        return args -> {
            System.out.println("\n🚀 [STARTUP] Initialisation des données de base...\n");
            categorieService.initialiserCategories();
            System.out.println("\n✅ [STARTUP] Initialisation terminée\n");
        };
    }
}