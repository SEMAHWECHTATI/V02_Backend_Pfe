package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Intervention.ConfigurationGlobale;
import com.sagem.g2ii.Repository.ConfigurationGlobaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationGlobaleService {

    @Autowired
    private ConfigurationGlobaleRepository repository;

    // Récupérer la config (ou en créer une par défaut si la table est vide)
    public ConfigurationGlobale obtenirConfiguration() {
        return repository.findById(1L).orElseGet(() -> {
            ConfigurationGlobale defaut = ConfigurationGlobale.builder()
                    .id(1L)
                    .indiceFaisabiliteEquipe(10.0)
                    .alertesEmailActives(true)
                    .autoAssignationActive(true)
                    .build();
            return repository.save(defaut);
        });
    }

    // Mettre à jour la config
    public ConfigurationGlobale sauvegarderConfiguration(ConfigurationGlobale nouvelleConfig) {
        nouvelleConfig.setId(1L); // On force l'ID 1 pour écraser l'ancienne
        return repository.save(nouvelleConfig);
    }
}