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

/**
 * ✅ Service complet pour la gestion des Catégories et leurs SLA associés
 */
@Service
public class CategorieService {

    @Autowired
    private CategorieRepo categorieRepository;

    @Autowired
    private IntGroupe groupeRepository;

    /**
     * 🔧 Initialiser les catégories par défaut avec leurs SLA
     * À appeler une seule fois au démarrage de l'application
     */
    @Transactional
    public void initialiserCategories() {
        System.out.println("\n🔧 [INIT CATÉGORIES] Initialisation des catégories par défaut\n");

        // ✅ ÉTAPE 1 : Récupérer ou créer les groupes existants
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
                2  // Délai base en heures
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

        System.out.println("\n✅ [INIT CATÉGORIES] Catégories initialisées avec succès\n");
    }

    /**
     * ✅ Créer une catégorie avec ses 4 SLA (Basse, Moyenne, Haute, Critique)
     *
     * Pour chaque priorité, les délais sont calculés proportionnellement au délai de base
     * @param nomCategorie Nom de la catégorie
     * @param description Description détaillée
     * @param type Type de ticket associé
     * @param groupeResponsable Groupe responsable de cette catégorie
     * @param delaiBaseHeures Délai de base en heures (pour la priorité Moyenne)
     */
    @Transactional
    private void creerCategorieAvecSLA(
            String nomCategorie,
            String description,
            TypeTicket type,
            Groupe groupeResponsable,
            int delaiBaseHeures) {

        System.out.println("📋 Création: " + nomCategorie);

        // ✅ VALIDATION: Vérifier si la catégorie existe déjà
        Optional<Categorie> existante = categorieRepository.findByType(type);

        if (existante.isPresent()) {
            System.out.println("   ℹ️ Catégorie déjà existante - ignorée");
            return;
        }

        // ✅ CRÉER L'OBJET CATÉGORIE
        Categorie categorie = new Categorie();
        categorie.setNomCategorie(nomCategorie);
        categorie.setDescriptionCategorie(description);
        categorie.setType(type);
        categorie.setGroupeResponsable(groupeResponsable);
        categorie.setActif(true);

        // ✅ CRÉER LES 4 SLA (Basse, Moyenne, Haute, Critique)
        List<SLA> slas = new ArrayList<>();

        // 📌 SLA Basse Priorité: Délai x2
        slas.add(SLA.builder()
                .nomSLA("SLA Basse Priorité")
                .delaiResolutionHeure(delaiBaseHeures * 2)  // 2x le délai de base
                .delaiPriseEnChargeHeure(24)                 // 24h pour prise en charge
                .priorite(Priorite.Basse)
                .categorie(categorie)
                .build());

        // 📌 SLA Moyenne Priorité: Délai de base
        slas.add(SLA.builder()
                .nomSLA("SLA Moyenne Priorité")
                .delaiResolutionHeure(delaiBaseHeures)      // Délai de base
                .delaiPriseEnChargeHeure(8)                  // 8h pour prise en charge
                .priorite(Priorite.Moyenne)
                .categorie(categorie)
                .build());

        // 📌 SLA Haute Priorité: Délai / 2
        slas.add(SLA.builder()
                .nomSLA("SLA Haute Priorité")
                .delaiResolutionHeure(Math.max(delaiBaseHeures / 2, 1))  // Moitié du délai (min 1h)
                .delaiPriseEnChargeHeure(4)                  // 4h pour prise en charge
                .priorite(Priorite.Haute)
                .categorie(categorie)
                .build());

        // 📌 SLA Critique: Délai ultra court
        slas.add(SLA.builder()
                .nomSLA("SLA Critique")
                .delaiResolutionHeure(1)                     // 1h max
                .delaiPriseEnChargeHeure(1)                  // 1h max pour prise en charge
                .priorite(Priorite.Critique)
                .categorie(categorie)
                .build());

        // ✅ ASSIGNER LES SLA À LA CATÉGORIE
        categorie.setSlas(slas);

        // ✅ SAUVEGARDER EN BASE
        Categorie categorieSauvegardee = categorieRepository.save(categorie);

        System.out.println("   ✅ Catégorie créée (Groupe: " + groupeResponsable.getNomGroupes() + ")");
        System.out.println("      └─ 4 SLA créés (Basse, Moyenne, Haute, Critique)");
    }

    /**
     * ✅ Récupérer ou créer un groupe s'il n'existe pas
     */
    @Transactional
    private Groupe getOrCreateGroupe(GroupeTechnicien groupeEnum) {
        // ✅ Chercher le groupe par nom
        Optional<Groupe> existant = groupeRepository.findAll().stream()
                .filter(g -> g.getNomGroupes().equals(groupeEnum))
                .findFirst();

        if (existant.isPresent()) {
            return existant.get();
        }

        // ✅ Créer le groupe s'il n'existe pas
        System.out.println("   👥 Création du groupe: " + groupeEnum.name());

        Groupe groupe = new Groupe();
        groupe.setNomGroupes(groupeEnum);
        groupe.setDescription("Groupe technique: " + groupeEnum.name());
        groupe.setActif(true);

        return groupeRepository.save(groupe);
    }

    // ============================================================================
    // MÉTHODES PUBLIQUES
    // ============================================================================

    /**
     * 📋 Lister toutes les catégories actives
     */
    public List<Categorie> listerCategories() {
        System.out.println("📋 [LISTER CATÉGORIES] Récupération de toutes les catégories actives");
        List<Categorie> categories = categorieRepository.findByActifTrue();
        System.out.println("   Total: " + categories.size() + " catégorie(s)");
        return categories;
    }

    /**
     * 🔍 Récupérer une catégorie par ID
     */
    public Categorie getCategorie(Long id) {
        System.out.println("🔎 [RÉCUPÉRER CATÉGORIE] ID: " + id);
        return categorieRepository.findById(id)
                .orElseThrow(() -> {
                    System.err.println("   ❌ Catégorie non trouvée");
                    return new RuntimeException("Catégorie non trouvée avec l'ID: " + id);
                });
    }

    /**
     * 🔍 Récupérer une catégorie par TypeTicket
     */
    public Categorie getCategorieParType(TypeTicket type) {
        System.out.println("🔎 [RÉCUPÉRER CATÉGORIE] Type: " + type);
        return categorieRepository.findByType(type)
                .orElseThrow(() -> {
                    System.err.println("   ❌ Aucune catégorie trouvée pour ce type");
                    return new RuntimeException("Catégorie non trouvée pour type: " + type);
                });
    }

    /**
     * 📍 Récupérer le groupe responsable d'une catégorie
     */
    public Groupe getGroupeResponsable(Long idCategorie) {
        System.out.println("👥 [GROUPE RESPONSABLE] Catégorie ID: " + idCategorie);
        Categorie categorie = getCategorie(idCategorie);
        return categorie.getGroupeResponsable();
    }

    /**
     * 🔗 Récupérer les SLA d'une catégorie
     */
    public List<SLA> getSLAsCategorie(Long idCategorie) {
        System.out.println("📊 [SLA CATÉGORIE] Catégorie ID: " + idCategorie);
        Categorie categorie = getCategorie(idCategorie);
        List<SLA> slas = categorie.getSlas();
        System.out.println("   Total SLA: " + (slas != null ? slas.size() : 0));
        return slas;
    }

    // ============================================================================
    // OPÉRATIONS CRUD
    // ============================================================================

    /**
     * ➕ Créer ou mettre à jour une catégorie
     */
    @Transactional
    public Categorie creerCategorie(Categorie categorie) {
        System.out.println("➕ [CRÉER CATÉGORIE] " + categorie.getNomCategorie());
        Categorie saved = categorieRepository.save(categorie);
        System.out.println("   ✅ Sauvegardée (ID: " + saved.getIdCategorie() + ")");
        return saved;
    }

    /**
     * ✏️ Mettre à jour une catégorie
     */
    @Transactional
    public Categorie mettreAJourCategorie(Long id, Categorie categorieUpdated) {
        System.out.println("✏️ [METTRE À JOUR CATÉGORIE] ID: " + id);
        Categorie categorie = getCategorie(id);

        if (categorieUpdated.getNomCategorie() != null) {
            categorie.setNomCategorie(categorieUpdated.getNomCategorie());
        }
        if (categorieUpdated.getDescriptionCategorie() != null) {
            categorie.setDescriptionCategorie(categorieUpdated.getDescriptionCategorie());
        }

        Categorie saved = categorieRepository.save(categorie);
        System.out.println("   ✅ Mise à jour effectuée");
        return saved;
    }

    /**
     * 🗂️ Archiver une catégorie (Soft Delete)
     * Passe l'état actif à false au lieu de supprimer physiquement
     */
    @Transactional
    public Categorie archiverCategorie(Long id) {
        System.out.println("🗂️ [ARCHIVER CATÉGORIE] ID: " + id);
        Categorie categorie = getCategorie(id);
        categorie.setActif(false);
        Categorie saved = categorieRepository.save(categorie);
        System.out.println("   ✅ Catégorie archivée");
        return saved;
    }

    /**
     * 🔄 Réactiver une catégorie archivée
     */
    @Transactional
    public Categorie reactiverCategorie(Long id) {
        System.out.println("🔄 [RÉACTIVER CATÉGORIE] ID: " + id);
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        categorie.setActif(true);
        Categorie saved = categorieRepository.save(categorie);
        System.out.println("   ✅ Catégorie réactivée");
        return saved;
    }

    /**
     * 🔍 Vérifier si une catégorie existe par type
     */
    public boolean existeParType(TypeTicket type) {
        return categorieRepository.findByType(type).isPresent();
    }
}
