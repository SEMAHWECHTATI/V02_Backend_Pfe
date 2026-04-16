package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Enumeration.TypeTicket;
import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Entity.Intervention.SLA;
import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
import com.sagem.g2ii.Repository.CategorieRepo;
import com.sagem.g2ii.Repository.IntGroupe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategorieService {

    @Autowired
    private CategorieRepo categorieRepository;

    @Autowired
    private IntGroupe groupeRepository;

    /**
     * 🔧 Initialiser les catégories par défaut
     */
    @Transactional
    public void initialiserCategories() {
        System.out.println("\n🔧 Initialisation des catégories par défaut\n");

        // ✅ ÉTAPE 1 : Récupérer les groupes existants
        Groupe groupeReseaux = getOrCreateGroupe(GroupeTechnicien.IT_Reseaux_Informatique);
        Groupe groupeMaintenance = getOrCreateGroupe(GroupeTechnicien.IT_Maintenance_Informatique);
        Groupe groupeStock = getOrCreateGroupe(GroupeTechnicien.IT_Gestionnaire_Stock);
        Groupe groupeService = getOrCreateGroupe(GroupeTechnicien.IT_Tracabilite_Produit);
        Groupe groupeManagement = getOrCreateGroupe(GroupeTechnicien.IT_Management);

        // ✅ ÉTAPE 2 : Créer les catégories avec leurs SLA
        creerCategorieAvecSLA(
                "Panne Réseau",
                "Problèmes de connectivité, perte d'accès internet, routeur, switch",
                TypeTicket.INTERVENTION_RESEAUX,
                groupeReseaux,
                2
        );

        creerCategorieAvecSLA(
                "Intervention Informatique",
                "Maintenance préventive, réparation, mise à jour logicielle",
                TypeTicket.INTERVENTION_INFORMATIQUE,
                groupeMaintenance,
                8
        );

        creerCategorieAvecSLA(
                "Demande de Matériel",
                "Ordinateur, écran, clavier, souris, périphériques",
                TypeTicket.DEMANDE_MATERIEL,
                groupeStock,
                120
        );

        creerCategorieAvecSLA(
                "Demande de Service IT",
                "Création de compte, droits d'accès, configuration logicielle",
                TypeTicket.DEMANDE_SERVICE,
                groupeService,
                4
        );

        creerCategorieAvecSLA(
                "Incident Critique",
                "Problème affectant plusieurs utilisateurs ou services critiques",
                TypeTicket.INCIDENT_CRITIQUE,
                groupeReseaux,
                1
        );

        creerCategorieAvecSLA(
                "Dérogation",
                "Demande d'exception ou d'autorisation spéciale",
                TypeTicket.DEROGATION,
                groupeManagement,
                48
        );

        creerCategorieAvecSLA(
                "Amélioration",
                "Suggestion d'optimisation ou nouvelle fonctionnalité",
                TypeTicket.AMELIORATION,
                groupeManagement,
                240
        );

        System.out.println("\n✅ Catégories initialisées\n");
    }

    /**
     * ✅ Créer une catégorie avec ses SLA
     */
    @Transactional
    private void creerCategorieAvecSLA(
            String nomCategorie,
            String description,
            TypeTicket type,
            Groupe groupeResponsable,
            int delaiBaseHeures) {

        System.out.println("📋 " + nomCategorie);

        // Vérifier si la catégorie existe déjà
        Optional<Categorie> existante = categorieRepository.findByType(type);

        if (existante.isPresent()) {
            System.out.println("   ✓ Déjà existante");
            return;
        }

        Categorie categorie = new Categorie();
        categorie.setNomCategorie(nomCategorie);
        categorie.setDescriptionCategorie(description);
        categorie.setType(type);
        categorie.setGroupeResponsable(groupeResponsable);
        categorie.setActif(true);

        // Créer les SLA
        List<SLA> slas = new ArrayList<>();

        slas.add(SLA.builder()
                .nomSLA("SLA Basse")
                .delaiResolutionHeure(delaiBaseHeures * 2)
                .delaiPriseEnchargeHeur(24)
                .priorite(Priorite.Basse)
                .categorie(categorie)
                .build());

        slas.add(SLA.builder()
                .nomSLA("SLA Moyenne")
                .delaiResolutionHeure(delaiBaseHeures)
                .delaiPriseEnchargeHeur(8)
                .priorite(Priorite.Moyenne)
                .categorie(categorie)
                .build());

        slas.add(SLA.builder()
                .nomSLA("SLA Haute")
                .delaiResolutionHeure(Math.max(delaiBaseHeures / 2, 1))
                .delaiPriseEnchargeHeur(4)
                .priorite(Priorite.Haute)
                .categorie(categorie)
                .build());

        slas.add(SLA.builder()
                .nomSLA("SLA Critique")
                .delaiResolutionHeure(1)
                .delaiPriseEnchargeHeur(1)
                .priorite(Priorite.Critique)
                .categorie(categorie)
                .build());

        categorie.setSlas(slas);
        categorieRepository.save(categorie);

        System.out.println("   ✅ Créée (Groupe: " + groupeResponsable.getNomGroupes() + ")");
    }

    /**
     * ✅ Récupérer ou créer un groupe
     */
    @Transactional
    private Groupe getOrCreateGroupe(GroupeTechnicien groupeEnum) {
        String nomGroupe = groupeEnum.name();

        Optional<Groupe> existant = groupeRepository.findAll().stream()
                .filter(g -> g.getNomGroupes().equals(groupeEnum))
                .findFirst();

        if (existant.isPresent()) {
            return existant.get();
        }

        Groupe groupe = new Groupe();
        groupe.setNomGroupes(groupeEnum);
        groupe.setDescription("Groupe: " + groupeEnum.name());
        groupe.setActif(true);

        return groupeRepository.save(groupe);
    }

    /**
     * 📋 Lister toutes les catégories
     */
    public List<Categorie> listerCategories() {
        return categorieRepository.findByActifTrue();
    }

    /**
     * 🔍 Récupérer une catégorie par ID
     */
    public Categorie getCategorie(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
    }

    /**
     * 🔍 Récupérer une catégorie par TypeTicket
     */
    public Categorie getCategorieParType(TypeTicket type) {
        return categorieRepository.findByType(type)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée pour type: " + type));
    }

    /**
     * 📍 Récupérer le groupe responsable
     */
    public Groupe getGroupeResponsable(Long idCategorie) {
        Categorie categorie = getCategorie(idCategorie);
        return categorie.getGroupeResponsable();
    }

    // ==========================================
    // 👇 NOUVELLES MÉTHODES AJOUTÉES ICI 👇
    // ==========================================

    /**
     * ➕ Créer ou mettre à jour une catégorie
     * (Sauvegarde l'objet dans la base de données)
     */
    @Transactional
    public Categorie creerCategorie(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    /**
     * 📦 Archiver une catégorie (Soft Delete)
     * (Passe l'état actif à false)
     */
    @Transactional
    public Categorie archiverCategorie(Long id) {
        Categorie categorie = getCategorie(id);
        categorie.setActif(false);
        return categorieRepository.save(categorie);
    }
}