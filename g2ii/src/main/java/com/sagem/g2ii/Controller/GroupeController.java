package com.sagem.g2ii.Controller;


import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Repository.IntGroupe;
import com.sagem.g2ii.Service.GroupeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groupes")
@CrossOrigin("*")
public class GroupeController {

    @Autowired
    private GroupeService groupeservice;

    @Autowired
    private IntGroupe groupeRepo;

    // 1. Lister tous les groupes (ex: Maintenance, Réseaux, etc.)
    @GetMapping("/all")
    public List<Groupe> getAllGroupes() {
        return groupeservice.getAllGroupe();
    }

    // 2. Créer un nouveau groupe technique
    @PostMapping("/creer")
    public Groupe creerGroupe(@RequestBody Groupe groupe) {
        return groupeservice.ajouterGroup(groupe);
    }

    // 3. Récupérer un groupe avec ses membres (ses utilisateurs)
    @GetMapping("/{id}")
    public Groupe getGroupeById(@PathVariable Long id) {
        return groupeservice.getGroupeById(id);

    }

    // 4. Supprimer un groupe (si plus utilisé)
    @DeleteMapping("/supprimer/{id}")
    public void supprimerGroupe(@PathVariable Long id) {
         groupeservice.deleteGroupe(id);
    }
}
