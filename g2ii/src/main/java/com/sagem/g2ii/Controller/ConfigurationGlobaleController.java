package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Intervention.ConfigurationGlobale;
import com.sagem.g2ii.Service.ConfigurationGlobaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
@CrossOrigin("*") // À adapter selon votre sécurité
public class ConfigurationGlobaleController {

    @Autowired
    private ConfigurationGlobaleService service;

    @GetMapping
    public ResponseEntity<ConfigurationGlobale> getConfig() {
        return ResponseEntity.ok(service.obtenirConfiguration());
    }

    @PutMapping
    public ResponseEntity<ConfigurationGlobale> updateConfig(@RequestBody ConfigurationGlobale config) {
        return ResponseEntity.ok(service.sauvegarderConfiguration(config));
    }
}