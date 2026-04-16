package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Enumeration.TypeTicket;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Repository.IntGroupe;
import com.sagem.g2ii.Service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
public class CategorieController {

    @Autowired
    private CategorieService categorieService;

    @Autowired
    private IntGroupe groupeRepository;

    /**
     * 🔧 Initialiser les catégories par défaut
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initialiserCategories() {
        try {
            System.out.println("\n📌 Initialisation des catégories");
            categorieService.initialiserCategories();
            return ResponseEntity.ok(Map.of("message", "✅ Catégories initialisées avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📋 Lister toutes les catégories (CORRIGÉ POUR ANGULAR ET SWAGGER)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Categorie>> listerCategories() {
        try {
            System.out.println("\n📌 Récupération de toutes les catégories");
            List<Categorie> categories = categorieService.listerCategories();

            // Retourne directement la liste (Tableau JSON)
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            System.err.println("❌ Erreur listing: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🔍 Récupérer une catégorie par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Categorie> getCategorie(@PathVariable Long id) {
        try {
            Categorie categorie = categorieService.getCategorie(id);
            return ResponseEntity.ok(categorie);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * ➕ AJOUTER une nouvelle catégorie (CORRIGÉ POUR LA BASE DE DONNÉES)
     */
    @PostMapping("/ajouter")
    public ResponseEntity<Categorie> ajouterCategorie(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("\n📌 Ajout d'une nouvelle catégorie");

            String nomCategorie = (String) request.get("nomCategorie");
            String descriptionCategorie = (String) request.get("descriptionCategorie");
            String type = (String) request.get("type");
            Long groupeId = request.get("groupeId") != null ? Long.valueOf(request.get("groupeId").toString()) : null;

            TypeTicket enumType = TypeTicket.valueOf(type);
            Groupe groupe = null;
            if (groupeId != null) {
                groupe = groupeRepository.findById(groupeId).orElse(null);
            }

            Categorie categorie = new Categorie();
            categorie.setNomCategorie(nomCategorie);
            categorie.setDescriptionCategorie(descriptionCategorie != null ? descriptionCategorie : "");
            categorie.setType(enumType);
            categorie.setActif(true);
            categorie.setGroupeResponsable(groupe);

            // ✅ SAUVEGARDE ACTIVÉE !
            Categorie categorieSauvegardee = categorieService.creerCategorie(categorie);

            return ResponseEntity.status(HttpStatus.CREATED).body(categorieSauvegardee);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✏️ MODIFIER une catégorie
     */
    @PutMapping("/{id}")
    public ResponseEntity<Categorie> modifierCategorie(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            System.out.println("\n📌 Modification catégorie ID: " + id);
            Categorie categorie = categorieService.getCategorie(id);

            if (request.get("nomCategorie") != null) {
                categorie.setNomCategorie((String) request.get("nomCategorie"));
            }
            if (request.get("descriptionCategorie") != null) {
                categorie.setDescriptionCategorie((String) request.get("descriptionCategorie"));
            }
            if (request.get("actif") != null) {
                categorie.setActif((Boolean) request.get("actif"));
            }

            // ✅ SAUVEGARDE ACTIVÉE !
            Categorie categorieModifiee = categorieService.creerCategorie(categorie);

            return ResponseEntity.ok(categorieModifiee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 📦 ARCHIVER une catégorie
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Categorie> archiverCategorie(@PathVariable Long id) {
        try {
            System.out.println("\n📌 Archivage catégorie ID: " + id);
            // ✅ APPEL À LA MÉTHODE ARCHIVER DU SERVICE
            Categorie categorieArchivee = categorieService.archiverCategorie(id);
            return ResponseEntity.ok(categorieArchivee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 🔍 RECHERCHER les catégories par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Categorie> getCategorieParType(@PathVariable String type) {
        try {
            TypeTicket enumType = TypeTicket.valueOf(type);
            Categorie categorie = categorieService.getCategorieParType(enumType);
            return ResponseEntity.ok(categorie);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 📊 OBTENIR les statistiques des catégories
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        try {
            List<Categorie> toutes = categorieService.listerCategories();
            long actives = toutes.stream().filter(Categorie::getActif).count();
            long inactives = toutes.size() - actives;

            return ResponseEntity.ok(Map.of(
                    "total", toutes.size(),
                    "actives", actives,
                    "inactives", inactives
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📋 LISTER les types de tickets disponibles
     */
    @GetMapping("/types/all")
    public ResponseEntity<TypeTicket[]> getTypesDisponibles() {
        try {
            // Renvoie directement le tableau des types pour Angular
            return ResponseEntity.ok(TypeTicket.values());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}