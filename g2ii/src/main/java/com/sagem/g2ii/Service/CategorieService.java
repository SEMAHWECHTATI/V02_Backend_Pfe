package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.TypeTicket;
import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Entity.Intervention.SLA;
import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Repository.CategorieRepo;
import com.sagem.g2ii.Repository.IntGroupe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ✅ Service complet pour la gestion des Catégories et leurs SLA associés (Audit-LOG inclus)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategorieService {

    private final CategorieRepo categorieRepository;
    private final IntGroupe groupeRepository;
    private final JournalAuditService journalAuditService;

    /**
     * Helper pour récupérer l'utilisateur actuellement connecté via Spring Security Context
     */
    private Utilisateur getConnectedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Utilisateur) {
                return (Utilisateur) principal;
            }
        } catch (Exception e) {
            // Contexte asynchrone ou démarrage de l'app (système)
        }
        return null;
    }

    /**
     * 🔧 Initialiser les catégories par défaut avec leurs SLA
     */
    @Transactional
    public void initialiserCategories() {
        // 🎯 On vérifie si les catégories système existent déjà globalement
        // pour éviter d'exécuter des requêtes inutiles et d'écrire de faux logs d'audit.
        if (categorieRepository.count() > 0) {
            log.info("✅ [INIT CATÉGORIES] Les catégories système sont déjà présentes en base de données. Saut de l'initialisation.");
            return;
        }

        log.info("🔧 [INIT CATÉGORIES] Initialisation des catégories par défaut...");

        Groupe groupeReseaux = getOrCreateGroupe(GroupeTechnicien.IT_Reseaux_Informatique);
        Groupe groupeMaintenance = getOrCreateGroupe(GroupeTechnicien.IT_Maintenance_Informatique);
        Groupe groupeStock = getOrCreateGroupe(GroupeTechnicien.IT_Gestionnaire_Stock);
        Groupe groupeService = getOrCreateGroupe(GroupeTechnicien.IT_Tracabilite_Produit);
        Groupe groupeManagement = getOrCreateGroupe(GroupeTechnicien.IT_Management);

        creerCategorieAvecSLA("Panne Réseau", "Problèmes de connectivité, perte d'accès internet", TypeTicket.INTERVENTION_RESEAUX, groupeReseaux, 2);
        creerCategorieAvecSLA("Intervention Informatique", "Maintenance préventive, réparation", TypeTicket.INTERVENTION_INFORMATIQUE, groupeMaintenance, 8);
        creerCategorieAvecSLA("Demande de Matériel", "Ordinateur, écran, clavier", TypeTicket.DEMANDE_MATERIEL, groupeStock, 120);
        creerCategorieAvecSLA("Demande de Service IT", "Création de compte, droits d'accès", TypeTicket.DEMANDE_SERVICE, groupeService, 4);
        creerCategorieAvecSLA("Incident Critique", "Problème affectant plusieurs utilisateurs", TypeTicket.INCIDENT_CRITIQUE, groupeReseaux, 1);
        creerCategorieAvecSLA("Dérogation", "Demande d'exception ou d'autorisation spéciale", TypeTicket.DEROGATION, groupeManagement, 48);
        creerCategorieAvecSLA("Amélioration", "Suggestion d'optimisation", TypeTicket.AMELIORATION, groupeManagement, 240);

        // 🌟 LOG SYSTEME GLOBALE : Ne s'exécutera désormais qu'au premier lancement de l'application !
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.SYSTEME,
                ActionAudit.APPROBATION_DEMANDE, // Remplaçable par ActionAudit.UPDATE_CONFIGURATION si souhaité
                "Configuration",
                null,
                "Initialisation automatique réussie des catégories système et des profils SLA.",
                null, null, NiveauAudit.INFO, true, null
        );

        log.info("✅ [INIT CATÉGORIES] Catégories initialisées avec succès");
    }

    @Transactional
    protected void creerCategorieAvecSLA(String nomCategorie, String description, TypeTicket type, Groupe groupeResponsable, int delaiBaseHeures) {
        List<Categorie> existantes = categorieRepository.findByType(type);

        if (!existantes.isEmpty()) {
            return;
        }

        Categorie categorie = new Categorie();
        categorie.setNomCategorie(nomCategorie);
        categorie.setDescriptionCategorie(description);
        categorie.setType(type);
        categorie.setGroupeResponsable(groupeResponsable);
        categorie.setActif(true);

        List<SLA> slas = new ArrayList<>();
        slas.add(SLA.builder().nomSLA("SLA Basse Priorité").delaiResolutionHeure(delaiBaseHeures * 2.0).delaiPriseEnChargeHeure(24.0).priorite(Priorite.Basse).categorie(categorie).build());
        slas.add(SLA.builder().nomSLA("SLA Moyenne Priorité").delaiResolutionHeure((double) delaiBaseHeures).delaiPriseEnChargeHeure(8.0).priorite(Priorite.Moyenne).categorie(categorie).build());
        slas.add(SLA.builder().nomSLA("SLA Haute Priorité").delaiResolutionHeure(Math.max((double) delaiBaseHeures / 2.0, 1.0)).delaiPriseEnChargeHeure(4.0).priorite(Priorite.Haute).categorie(categorie).build());
        slas.add(SLA.builder().nomSLA("SLA Critique").delaiResolutionHeure(1.0).delaiPriseEnChargeHeure(1.0).priorite(Priorite.Critique).categorie(categorie).build());

        categorie.setSlas(slas);
        Categorie saved = categorieRepository.save(categorie);

        // 🌟 LOG D'AUDIT : Enregistrement de la création automatique
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.GESTION_APPLICATION,
                ActionAudit.APPROBATION_DEMANDE,
                "Categorie",
                saved.getIdCategorie(),
                "Création automatique de la catégorie '" + nomCategorie + "' avec ses 4 règles SLA.",
                null,
                String.format("{type:'%s', groupe:'%s'}", type, groupeResponsable.getNomGroupes()),
                NiveauAudit.INFO, true, getConnectedUser()
        );
    }

    @Transactional
    protected Groupe getOrCreateGroupe(GroupeTechnicien groupeEnum) {
        Optional<Groupe> existant = groupeRepository.findAll().stream()
                .filter(g -> g.getNomGroupes().equals(groupeEnum))
                .findFirst();

        if (existant.isPresent()) {
            return existant.get();
        }

        Groupe groupe = new Groupe();
        groupe.setNomGroupes(groupeEnum);
        groupe.setDescription("Groupe technique: " + groupeEnum.name());
        groupe.setActif(true);

        // Audit de création du groupe technique
        Groupe savedGroupe = groupeRepository.save(groupe);
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.UTILISATEUR,
                ActionAudit.CREATE_GROUP,
                "Groupe",
                savedGroupe.getId(),
                "Création automatique du groupe de techniciens : " + groupeEnum.name(),
                null, null, NiveauAudit.INFO, true, null
        );

        return savedGroupe;
    }

    // ============================================================================
    // OPÉRATIONS CRUD MANUELLES (DASHBOARD ADMIN)
    // ============================================================================

    /**
     * ➕ Créer une nouvelle catégorie via le Dashboard Admin
     */
    @Transactional
    public Categorie creerCategorie(Categorie categorie) {
        log.info("➕ [CRÉER CATÉGORIE] " + categorie.getNomCategorie());
        Categorie saved = categorieRepository.save(categorie);

        // 🌟 LOG D'AUDIT : Utilisation de APPROBATION_DEMANDE ou une action sur mesure
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.GESTION_APPLICATION,
                ActionAudit.APPROBATION_DEMANDE,
                "Categorie",
                saved.getIdCategorie(),
                "Création manuelle de la catégorie : " + saved.getNomCategorie(),
                null,
                String.format("{nom:'%s', actif:%b}", saved.getNomCategorie(), saved.getActif()),
                NiveauAudit.INFO, true, getConnectedUser()
        );

        return saved;
    }

    /**
     * ✏️ Mettre à jour les informations d'une catégorie
     */
    @Transactional
    public Categorie mettreAJourCategorie(Long id, Categorie categorieUpdated) {
        log.info("✏️ [METTRE À JOUR CATÉGORIE] ID: " + id);
        Categorie categorie = getCategorie(id);

        String ancienNom = categorie.getNomCategorie();

        if (categorieUpdated.getNomCategorie() != null) {
            categorie.setNomCategorie(categorieUpdated.getNomCategorie());
        }
        if (categorieUpdated.getDescriptionCategorie() != null) {
            categorie.setDescriptionCategorie(categorieUpdated.getDescriptionCategorie());
        }

        Categorie saved = categorieRepository.save(categorie);

        // 🌟 LOG D'AUDIT : Remplacement de CHANGEMENT_MDP par UPDATE_CONFIGURATION ou équivalent
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.GESTION_APPLICATION,
                ActionAudit.UPDATE_CONFIGURATION,
                "Categorie",
                saved.getIdCategorie(),
                "Mise à jour de la catégorie ID " + id + " (Ancien nom: " + ancienNom + " -> Nouveau: " + saved.getNomCategorie() + ")",
                String.format("{nom:'%s'}", ancienNom),
                String.format("{nom:'%s'}", saved.getNomCategorie()),
                NiveauAudit.INFO, true, getConnectedUser()
        );

        return saved;
    }

    /**
     * 🗂️ Archiver une catégorie (Soft Delete)
     */
    @Transactional
    public Categorie archiverCategorie(Long id) {
        log.info("🗂️ [ARCHIVER CATÉGORIE] ID: " + id);
        Categorie categorie = getCategorie(id);
        categorie.setActif(false);
        Categorie saved = categorieRepository.save(categorie);

        // 🌟 LOG D'AUDIT
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.GESTION_APPLICATION,
                ActionAudit.BLOCAGE,
                "Categorie",
                saved.getIdCategorie(),
                "Archivage / Désactivation de la catégorie : " + saved.getNomCategorie(),
                "{actif:true}", "{actif:false}",
                NiveauAudit.WARNING, true, getConnectedUser()
        );

        return saved;
    }

    /**
     * 🔄 Réactiver une catégorie archivée
     */
    @Transactional
    public Categorie reactiverCategorie(Long id) {
        log.info("🔄 [RÉACTIVER CATÉGORIE] ID: " + id);
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        categorie.setActif(true);
        Categorie saved = categorieRepository.save(categorie);

        // 🌟 LOG D'AUDIT
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.GESTION_APPLICATION,
                ActionAudit.DEBLOCAGE,
                "Categorie",
                saved.getIdCategorie(),
                "Réactivation manuelle de la catégorie : " + saved.getNomCategorie(),
                "{actif:false}", "{actif:true}",
                NiveauAudit.INFO, true, getConnectedUser()
        );

        return saved;
    }

    public List<Categorie> listerCategories() { return categorieRepository.findByActifTrue(); }
    public Categorie getCategorie(Long id) { return categorieRepository.findById(id).orElseThrow(() -> new RuntimeException("Catégorie introuvable")); }
    public Categorie getCategorieParType(TypeTicket type) { List<Categorie> c = categorieRepository.findByType(type); if(c.isEmpty()) throw new RuntimeException("Type introuvable"); return c.get(0); }
    public Groupe getGroupeResponsable(Long id) { return getCategorie(id).getGroupeResponsable(); }
    public List<SLA> getSLAsCategorie(Long id) { return getCategorie(id).getSlas(); }
    public boolean existeParType(TypeTicket type) { return !categorieRepository.findByType(type).isEmpty(); }
}