package com.sagem.g2ii.Controller;


import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Repository.IntGroupe;
import com.sagem.g2ii.Service.GroupeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groupes")
@CrossOrigin("*")
public class GroupeController {

    @Autowired
    private GroupeService groupeservice;

    @Autowired
    private IntGroupe groupeRepo;

    // 1. Lister tous les groupes
    @GetMapping("/all")
    public List<Groupe> getAllGroupes() {
        List<Groupe> groupes = groupeservice.getAllGroupe();
        System.out.println("📍 Groupes retournés:");
        groupes.forEach(g -> System.out.println("   - " + g.getId() + ": " + g.getNomGroupes()));
        return groupes;
    }

    // 2. Créer un nouveau groupe
    @PostMapping("/creer")
    public Groupe creerGroupe(@RequestBody Groupe groupe) {
        return groupeservice.ajouterGroup(groupe);
    }

    // 3. Récupérer un groupe par ID
    @GetMapping("/{id}")
    public Groupe getGroupeById(@PathVariable Long id) {
        return groupeservice.getGroupeById(id);
    }

    // 4. Supprimer un groupe
    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Map<String, String>> supprimerGroupe(@PathVariable Long id) {
        groupeservice.deleteGroupe(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Groupe supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}