package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.SlaStatisticsDTO;
import com.sagem.g2ii.DTOs.TicketCreationDTO;
import com.sagem.g2ii.DTOs.TicketStatisticsDTO;
import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Entity.Intervention.SLA;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.CategorieRepo;
import com.sagem.g2ii.Repository.IntGroupe;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Repository.SLARepository;
import com.sagem.g2ii.Repository.TicketRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ✅ Service complet et corrigé pour la gestion des Tickets
 * Gère l'ensemble du cycle de vie des tickets avec calculs SLA précis
 */
@Service
public class TicketService {

    @Autowired
    private TicketRepo ticketRepository;

    @Autowired
    private CategorieRepo categorieRepository;

    @Autowired
    private IntUtilisateur utilisateurRepository;

    @Autowired
    private IntGroupe groupeRepository;

    @Autowired
    private SLARepository slaRepository;

    @Autowired
    private HistoriqueTicketService historiqueService;

    // ============================================================================
    // 1️⃣ CRÉER UN TICKET
    // ============================================================================
    /**
     * Créer un nouveau ticket depuis un DTO avec assignation automatique du SLA
     * @param dto TicketCreationDTO contenant les données du ticket
     * @return Ticket créé et sauvegardé
     */
    @Transactional
    public Ticket creerTicket(TicketCreationDTO dto) {
        System.out.println("📨 [CRÉER TICKET] Début de création");
        System.out.println("   Titre: " + dto.getTitre());

        // ✅ VALIDATION DES DONNÉES OBLIGATOIRES
        if (dto.getTitre() == null || dto.getTitre().trim().isEmpty()) {
            throw new RuntimeException("Le titre est requis");
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new RuntimeException("La description est requise");
        }

        // ✅ RÉCUPÉRER LES ENTITÉS LIÉES
        Utilisateur demandeur = utilisateurRepository.findById(dto.getDemandeurId())
                .orElseThrow(() -> new RuntimeException("Demandeur non trouvé avec l'ID: " + dto.getDemandeurId()));

        Categorie categorie = categorieRepository.findById(dto.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + dto.getCategorieId()));

        Groupe groupe = groupeRepository.findById(dto.getGroupeId())
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé avec l'ID: " + dto.getGroupeId()));

        System.out.println("✅ Entités récupérées:");
        System.out.println("   Demandeur: " + demandeur.getEmail());
        System.out.println("   Catégorie: " + categorie.getNomCategorie() + " (ID: " + categorie.getIdCategorie() + ")");
        System.out.println("   Groupe: " + groupe.getNomGroupes());

        // ✅ CRÉER L'OBJET TICKET
        Ticket ticket = new Ticket();
        ticket.setTitre(dto.getTitre().trim());
        ticket.setDescription(dto.getDescription().trim());

        // Assignation de la priorité (par défaut: Moyenne)
        Priorite prioriteAssignee = dto.getPriorite() != null ? dto.getPriorite() : Priorite.Moyenne;
        ticket.setPriorite(prioriteAssignee);

        // États initiaux
        ticket.setStatut(StatutTicket.Nouveau);
        ticket.setDate(LocalDateTime.now());
        ticket.setDemandeur(demandeur);
        ticket.setCategorie(categorie);
        ticket.setGroupeAssigne(groupe);

        // ✅ INITIALISER LES FLAGS SLA
        ticket.setSlaRespecte(true);
        ticket.setSlaPriseEnChargeRespecte(null);  // À déterminer lors du démarrage
        ticket.setSlaResolutionRespecte(null);      // À déterminer lors de la résolution

        // ✅ RECHERCHER ET ASSIGNER LE SLA AUTOMATIQUEMENT
        System.out.println("\n🔍 [RECHERCHE SLA] Catégorie: " + categorie.getNomCategorie() + " | Priorité: " + prioriteAssignee);

        SLA slaApplicable = obtenirSLA(categorie, prioriteAssignee);

        if (slaApplicable != null) {
            ticket.setSlaAssigne(slaApplicable);
            System.out.println("   ✅ SLA trouvé et assigné: " + slaApplicable.getNomSLA());
            System.out.println("      - ID SLA: " + slaApplicable.getIdSLA());
            System.out.println("      - Prise en charge: " + slaApplicable.getDelaiPriseEnChargeHeure() + "h");
            System.out.println("      - Résolution: " + slaApplicable.getDelaiResolutionHeure() + "h");
        } else {
            System.out.println("   ❌ AUCUN SLA TROUVÉ POUR CETTE COMBINAISON!");
            System.out.println("      → Cherche: Catégorie ID=" + categorie.getIdCategorie() + " + Priorité=" + prioriteAssignee);
            System.out.println("      → Vérifiez que des SLA existent en base avec cette combinaison");
            System.out.println("      → Le ticket sera créé SANS SLA");
        }

        // ✅ GÉNÉRER LA RÉFÉRENCE UNIQUE
        ticket.setReference(genererReference());
        System.out.println("📌 Référence générée: " + ticket.getReference());

        // ✅ SAUVEGARDER EN BASE
        Ticket ticketSauvegarde = ticketRepository.save(ticket);
        System.out.println("✅ Ticket créé avec l'ID: " + ticketSauvegarde.getIdTicket());
        System.out.println("   SLA assigné: " + (ticketSauvegarde.getSlaAssigne() != null ? ticketSauvegarde.getSlaAssigne().getNomSLA() : "AUCUN"));

        return ticketSauvegarde;
    }

    // ============================================================================
    // 2️⃣ GÉNÉRER UNE RÉFÉRENCE UNIQUE
    // ============================================================================
    /**
     * Génère une référence unique pour chaque ticket au format: SAGyyMMmm-nnnn
     * Exemple: SAG260608-0001
     */
    private String genererReference() {
        String prefixeDate = "SAG" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

        String derniereRef = ticketRepository.findTopByReferenceStartingWithOrderByReferenceDesc(prefixeDate)
                .map(Ticket::getReference)
                .orElse(null);

        int compteur = 1;
        if (derniereRef != null && derniereRef.contains("-")) {
            try {
                String suffixe = derniereRef.substring(derniereRef.lastIndexOf("-") + 1);
                compteur = Integer.parseInt(suffixe) + 1;
            } catch (NumberFormatException e) {
                compteur = 1;
            }
        }

        return String.format("%s-%04d", prefixeDate, compteur);
    }

    // ============================================================================
    // 3️⃣ DÉMARRER UN TICKET (Nouveau → En_Cours)
    // ============================================================================
    /**
     * Démarrer un ticket et calculer le SLA de prise en charge
     * @param idTicket ID du ticket à démarrer
     * @param idUtilisateur ID du technicien qui prend en charge
     * @return Ticket mis à jour
     */
    @Transactional
    public Ticket demarrerTicket(Long idTicket, Long idUtilisateur) {
        System.out.println("▶️ [DÉMARRER TICKET] ID: " + idTicket);

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + idTicket));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + idUtilisateur));

        // ✅ VALIDATION: Le ticket doit être dans l'état "Nouveau"
        if (ticket.getStatut() != StatutTicket.Nouveau) {
            throw new RuntimeException("Le ticket ne peut être démarré que depuis l'état NOUVEAU. État actuel: " + ticket.getStatut());
        }

        StatutTicket ancienStatut = ticket.getStatut();
        LocalDateTime dateDebut = LocalDateTime.now();

        ticket.setStatut(StatutTicket.En_Cours);
        ticket.setDatePriseEncharge(dateDebut);
        ticket.setTechnicienAssigne(utilisateur);

        // ✅ CALCUL DU SLA DE PRISE EN CHARGE
        if (ticket.getSlaAssigne() != null) {
            long heuresEcouleesPriseEnCharge = ChronoUnit.HOURS.between(
                    ticket.getDate(),
                    ticket.getDatePriseEncharge()
            );
            int delaiMaxPriseEnCharge = ticket.getSlaAssigne().getDelaiPriseEnChargeHeure();

            // ✅ CORRECTION: Utiliser < pour la conformité stricte
            boolean respectePriseEnCharge = heuresEcouleesPriseEnCharge < delaiMaxPriseEnCharge;
            ticket.setSlaPriseEnChargeRespecte(respectePriseEnCharge);

            if (!respectePriseEnCharge) {
                ticket.setSlaRespecte(false);  // Compromet le SLA global
            }

            System.out.println("   📊 SLA Prise en Charge:");
            System.out.println("      - Heures écoulées: " + heuresEcouleesPriseEnCharge + "h");
            System.out.println("      - Délai max autorisé: " + delaiMaxPriseEnCharge + "h");
            System.out.println("      - Respecté: " + (respectePriseEnCharge ? "✅ OUI" : "❌ NON"));
        } else {
            ticket.setSlaPriseEnChargeRespecte(true);
            System.out.println("   ⚠️ Aucun SLA assigné - prise en charge considérée conforme par défaut");
        }

        Ticket ticketUpdated = ticketRepository.save(ticket);

        // ✅ TRACER L'HISTORIQUE
        historiqueService.tracer(ticket, utilisateur, "Statut",
                ancienStatut.name(), StatutTicket.En_Cours.name());

        System.out.println("✅ Ticket démarré avec succès");

        return ticketUpdated;
    }

    // ============================================================================
    // 4️⃣ RÉSOUDRE UN TICKET (En_Cours → Resolu)
    // ============================================================================
    /**
     * Résoudre un ticket et calculer le SLA de résolution
     * @param idTicket ID du ticket
     * @param idUtilisateur ID du technicien qui résout
     * @param noteResolution Note de résolution (description de la solution)
     * @param delaiResolution Délai réel en minutes passé à résoudre le ticket
     * @return Ticket mis à jour avec calculs SLA
     */
    @Transactional
    public Ticket resoudreTicket(Long idTicket, Long idUtilisateur, String noteResolution, Integer delaiResolution) {
        System.out.println("✅ [RÉSOUDRE TICKET] ID: " + idTicket + " | Temps: " + delaiResolution + " min");

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + idTicket));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + idUtilisateur));

        // ✅ VALIDATION: Le ticket doit être En_Cours ou En_Attente
        if (ticket.getStatut() != StatutTicket.En_Cours && ticket.getStatut() != StatutTicket.En_Attente) {
            throw new RuntimeException("Le ticket doit être EN_COURS ou EN_ATTENTE. État actuel: " + ticket.getStatut());
        }

        StatutTicket ancienStatut = ticket.getStatut();
        LocalDateTime dateResolution = LocalDateTime.now();

        // ✅ METTRE À JOUR LES CHAMPS DE RÉSOLUTION
        ticket.setStatut(StatutTicket.Resolu);
        ticket.setDateResolution(dateResolution);
        ticket.setNoteResolution(noteResolution);
        ticket.setDelaiResolution(Double.valueOf(delaiResolution));

        // ✅ CALCULER LES SLA FINAUX
        calculerSLA(ticket);

        // ✅ SAUVEGARDER
        Ticket ticketUpdated = ticketRepository.save(ticket);

        // ✅ TRACER L'HISTORIQUE
        historiqueService.tracer(ticket, utilisateur, "Statut",
                ancienStatut.name(), StatutTicket.Resolu.name());

        historiqueService.tracer(ticket, utilisateur, "Délai Résolution",
                "N/A", delaiResolution + " minutes");

        historiqueService.tracer(ticket, utilisateur, "SLA Respecté",
                "N/A", ticket.getSlaRespecte() ? "OUI ✅" : "NON ❌");

        System.out.println("✅ Ticket résolu. SLA Global: " + (ticket.getSlaRespecte() ? "✅ RESPECTÉ" : "❌ DÉPASSÉ"));

        return ticketUpdated;
    }



    // ============================================================================
    // 5️⃣ CLÔTURER UN TICKET (Resolu → Cloture)
    // ============================================================================
    /**
     * Clôturer un ticket (action finale)
     * Seul l'admin ou le demandeur peut clôturer
     * @param idTicket ID du ticket
     * @param idUtilisateur ID de l'utilisateur (admin ou demandeur)
     * @param roleUtilisateur Rôle de l'utilisateur (ADMIN ou DEMANDEUR)
     * @return Ticket clôturé
     */
    @Transactional
    public Ticket cloturerTicket(Long idTicket, Long idUtilisateur, String roleUtilisateur) {
        System.out.println("🔒 [CLÔTURER TICKET] ID: " + idTicket);

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + idTicket));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + idUtilisateur));

        // ✅ VALIDATION: Vérifier les permissions
        boolean isAdmin = roleUtilisateur != null &&
                (roleUtilisateur.equalsIgnoreCase("ADMINISTRATEUR") ||
                        roleUtilisateur.equalsIgnoreCase("ADMIN"));

        boolean isDemandeur = ticket.getDemandeur().getId().equals(idUtilisateur);

        if (!isAdmin && !isDemandeur) {
            throw new RuntimeException("Seul l'administrateur ou le demandeur peut clôturer ce ticket");
        }

        // ✅ VALIDATION: Le ticket doit être RESOLU
        if (ticket.getStatut() != StatutTicket.Resolu) {
            throw new RuntimeException("Le ticket doit être RESOLU avant clôture. État actuel: " + ticket.getStatut());
        }

        StatutTicket ancienStatut = ticket.getStatut();
        LocalDateTime dateCloture = LocalDateTime.now();

        // ✅ METTRE À JOUR
        ticket.setStatut(StatutTicket.Cloture);
        ticket.setDateCloture(dateCloture);

        Ticket ticketUpdated = ticketRepository.save(ticket);

        // ✅ TRACER L'HISTORIQUE
        historiqueService.tracer(ticket, utilisateur, "Statut",
                ancienStatut.name(), StatutTicket.Cloture.name());

        System.out.println("✅ Ticket clôturé le " + LocalDate.now());

        return ticketUpdated;
    }

    public Ticket reouvrirTicket(Long idTicket, Long idUtilisateur) {
        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket introuvable"));

        // Vérification stricte de l'état pour la réouverture
        if (ticket.getStatut() != StatutTicket.Resolu) {
            throw new RuntimeException("Seul un ticket à l'état 'Resolu' peut être réouvert.");
        }

        // Changement d'état
        ticket.setStatut(StatutTicket.En_Cours);

        // Optionnel : tu peux ici enregistrer qui a réouvert le ticket ou vider la note de résolution précédente
        return ticketRepository.save(ticket);
    }
    // ============================================================================
    // 6️⃣ CALCULER LES SLA À LA RÉSOLUTION
    // ============================================================================
    /**
     * Calcule les indicateurs SLA:
     * - SLA Prise en Charge: délai entre création et prise en charge
     * - SLA Résolution: délai entre création et résolution
     * - SLA Global: AND des deux précédents
     */
    private void calculerSLA(Ticket ticket) {
        System.out.println("📊 [CALCULER SLA] Calcul approfondi des indicateurs");

        try {
            // ✅ VALIDATION: Dates obligatoires
            if (ticket.getDate() == null || ticket.getDateResolution() == null) {
                System.out.println("   ⚠️ Dates manquantes (création ou résolution). Abandon du calcul.");
                return;
            }

            // ============================================
            // 📌 CALCUL SLA RÉSOLUTION
            // ============================================
            long heuresTotalesResolution = ChronoUnit.HOURS.between(
                    ticket.getDate(),
                    ticket.getDateResolution()
            );

            System.out.println("   ⏱️ Heures totales (création → résolution): " + heuresTotalesResolution + "h");

            if (ticket.getSlaAssigne() != null) {
                SLA sla = ticket.getSlaAssigne();
                int delaiMaxResolution = sla.getDelaiResolutionHeure();

                // ✅ CORRECTION: Utiliser < pour éviter les dépassements égaux
                boolean respecteResolution = heuresTotalesResolution < delaiMaxResolution;
                ticket.setSlaResolutionRespecte(respecteResolution);

                // ============================================
                // 📌 CALCUL SLA GLOBAL
                // ============================================
                boolean respectePriseEnCharge = ticket.getSlaPriseEnChargeRespecte() != null
                        ? ticket.getSlaPriseEnChargeRespecte()
                        : true;

                // Le SLA global est respecté seulement si TOUS les paliers sont OK
                ticket.setSlaRespecte(respectePriseEnCharge && respecteResolution);

                System.out.println("   📊 SLA appliqué: " + sla.getNomSLA());
                System.out.println("   ✓ Prise en Charge respectée: " + (respectePriseEnCharge ? "✅ OUI" : "❌ NON"));
                System.out.println("   ✓ Résolution: " + heuresTotalesResolution + "h / Max " + delaiMaxResolution + "h");
                System.out.println("   ✓ Résolution respectée: " + (respecteResolution ? "✅ OUI" : "❌ NON"));
                System.out.println("   ⭐ SLA Global du ticket: " + (ticket.getSlaRespecte() ? "✅ RESPECTÉ" : "❌ DÉPASSÉ"));

            } else {
                // Aucun SLA assigné = conforme par défaut
                ticket.setSlaResolutionRespecte(true);
                boolean respectePriseEnCharge = ticket.getSlaPriseEnChargeRespecte() != null
                        ? ticket.getSlaPriseEnChargeRespecte()
                        : true;
                ticket.setSlaRespecte(respectePriseEnCharge);

                System.out.println("   ⚠️ Aucun SLA assigné - ticket considéré conforme par défaut");
            }

        } catch (Exception e) {
            System.err.println("   ❌ Erreur lors du calcul SLA: " + e.getMessage());
            e.printStackTrace();
            // Protection: ne pas bloquer la transaction
            ticket.setSlaRespecte(true);
        }
    }

    // ============================================================================
    // 7️⃣ OBTENIR LE SLA APPLICABLE
    // ============================================================================
    /**
     * Recherche le SLA applicable pour une catégorie et une priorité données
     * @param categorie La catégorie du ticket
     * @param priorite La priorité du ticket
     * @return SLA applicable ou null si aucun SLA ne correspond
     */
    public SLA obtenirSLA(Categorie categorie, Priorite priorite) {
        if (categorie == null || priorite == null) {
            System.out.println("   ❌ Recherche SLA avortée : Catégorie ou Priorité nulle.");
            return null;
        }

        System.out.println("🔍 [OBTENIR SLA] Recherche pour: " + categorie.getNomCategorie() + " | Priorité: " + priorite);

        // ✅ Utilisation de l'Optional pour éviter les NullPointerException de compilation
        Optional<SLA> slaApplicableOpt = slaRepository.findByCategorieAndPriorite(categorie, priorite);

        if (slaApplicableOpt.isPresent()) {
            SLA slaApplicable = slaApplicableOpt.get();
            System.out.println("   ✅ SLA trouvé: " + slaApplicable.getNomSLA() + " (ID: " + slaApplicable.getIdSLA() + ")");
            return slaApplicable;
        } else {
            System.out.println("   ❌ Aucun SLA trouvé!");
            System.out.println("      Catégorie: " + categorie.getNomCategorie() + " (ID: " + categorie.getIdCategorie() + ")"); // 💡 Retrait du shortValue()
            System.out.println("      Priorité: " + priorite);
            System.out.println("      → Vérifiez que la table SLA contient une ligne correspondant à cette combinaison");
            return null;
        }
    }    // ============================================================================
    // 8️⃣ LISTER LES TICKETS
    // ============================================================================

    /**
     * Lister TOUS les tickets
     */
    public List<Ticket> listerTous() {
        System.out.println("📋 [LISTER] Tous les tickets");
        return ticketRepository.findAll();
    }

    /**
     * Lister les tickets d'un demandeur spécifique
     */
    public List<Ticket> listerParDemandeur(Long idDemandeur) {
        System.out.println("📋 [LISTER] Tickets du demandeur: " + idDemandeur);
        return ticketRepository.findByDemandeur_Id(idDemandeur);
    }

    /**
     * Lister les tickets assignés à un groupe
     */
    public List<Ticket> listerParGroupe(Long idGroupe) {
        System.out.println("📋 [LISTER] Tickets du groupe: " + idGroupe);
        return ticketRepository.findByGroupeAssigne_Id(idGroupe);
    }

    /**
     * Lister les tickets par statut
     */
    public List<Ticket> listerParStatut(StatutTicket statut) {
        System.out.println("📋 [LISTER] Tickets avec statut: " + statut);
        return ticketRepository.findByStatut(statut);
    }

    /**
     * Lister les tickets par priorité
     */
    public List<Ticket> listerParPriorite(Priorite priorite) {
        System.out.println("📋 [LISTER] Tickets avec priorité: " + priorite);
        return ticketRepository.findByPriorite(priorite);
    }

    /**
     * Lister les tickets qui ont DÉPASSÉ leurs SLA
     */
    public List<Ticket> listerSLADepasses() {
        System.out.println("🚨 [LISTER] Tickets hors délai SLA");
        return ticketRepository.findBySlaRespecte(false);
    }

    // ============================================================================
    // 9️⃣ RÉCUPÉRER UN TICKET PAR ID
    // ============================================================================
    /**
     * Récupérer un ticket spécifique par son ID
     */
    public Ticket getTicketById(Long id) {
        System.out.println("🔎 [RÉCUPÉRER] Ticket ID: " + id);
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + id));
    }

    // ============================================================================
    // 🔟 SUPPRIMER UN TICKET
    // ============================================================================
    /**
     * Supprimer un ticket de la base de données
     */
    @Transactional
    public void supprimerTicket(Long id) {
        System.out.println("🗑️ [SUPPRIMER] Ticket ID: " + id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + id));

        ticketRepository.delete(ticket);
        System.out.println("✅ Ticket supprimé avec succès");
    }

    // ============================================================================
    // 1️⃣1️⃣ STATISTIQUES GÉNÉRALES DES TICKETS
    // ============================================================================
    /**
     * Retourner les statistiques globales des tickets
     */
    public TicketStatisticsDTO obtenirStatistiques() {
        System.out.println("📊 [STATISTIQUES] Génération des statistiques globales");

        long totalTickets = ticketRepository.findAll().size();
        long ticketsOuverts = ticketRepository.findByStatut(StatutTicket.Nouveau).size();
        long ticketsEnCours = ticketRepository.findByStatut(StatutTicket.En_Cours).size();
        long ticketsResolus = ticketRepository.findByStatut(StatutTicket.Resolu).size();
        long ticketsClotures = ticketRepository.findByStatut(StatutTicket.Cloture).size();
        long slaDepassesCount = ticketRepository.findBySlaRespecte(false).size();

        System.out.println("   Total: " + totalTickets);
        System.out.println("   Ouverts: " + ticketsOuverts);
        System.out.println("   En cours: " + ticketsEnCours);
        System.out.println("   Résolus: " + ticketsResolus);
        System.out.println("   Clôturés: " + ticketsClotures);
        System.out.println("   SLA dépassés: " + slaDepassesCount);

        return TicketStatisticsDTO.builder()
                .totalTickets(totalTickets)
                .ticketsOuverts(ticketsOuverts)
                .ticketsEnCours(ticketsEnCours)
                .ticketsResolus(ticketsResolus)
                .ticketsClotures(ticketsClotures)
                .slaDepassesCount(slaDepassesCount)
                .build();
    }

    // ============================================================================
    // 1️⃣2️⃣ STATISTIQUES SLA AVANCÉES (POUR LE DASHBOARD)
    // ============================================================================
    /**
     * Retourner les statistiques SLA détaillées pour le Dashboard
     * Inclut: taux de réussite global, par SLA, par priorité, etc.
     */
    public SlaStatisticsDTO obtenirStatistiquesSLA() {
        System.out.println("📊 [STATISTIQUES SLA] Génération des KPI pour le Dashboard");

        // ✅ Périmètre de base : Uniquement les tickets qualifiés avec un SLA actif
        long total = ticketRepository.countBySlaAssigneIsNotNull();
        System.out.println("   Tickets avec SLA: " + total);

        if (total == 0) {
            System.out.println("   ⚠️ Aucun ticket avec SLA - retour des statistiques vides");
            return new SlaStatisticsDTO();
        }

        // ✅ Compter les tickets conforme/non-conforme globaux (Basé sur le même périmètre)
        long respectes = ticketRepository.countBySlaAssigneIsNotNullAndSlaRespecte(true);
        long depasses = ticketRepository.countBySlaAssigneIsNotNullAndSlaRespecte(false);
        double tauxGlobal = ((double) respectes / total) * 100.0;

        System.out.println("   SLA Respectés: " + respectes);
        System.out.println("   SLA Dépassés: " + depasses);
        System.out.println("   Taux de réussite global: " + String.format("%.2f", tauxGlobal) + "%");

        // ✅ CORRIGÉ: Statistiques Prise en Charge alignées sur les tickets avec SLA
        long okPriseEnCharge = ticketRepository.countBySlaAssigneIsNotNullAndSlaPriseEnChargeRespecte(true);
        long koPriseEnCharge = ticketRepository.countBySlaAssigneIsNotNullAndSlaPriseEnChargeRespecte(false);

        System.out.println("   Prise en charge OK: " + okPriseEnCharge);
        System.out.println("   Prise en charge KO: " + koPriseEnCharge);

        // ✅ CORRIGÉ: Statistiques Résolution alignées sur les tickets avec SLA
        long okResolution = ticketRepository.countBySlaAssigneIsNotNullAndSlaResolutionRespecte(true);
        long koResolution = ticketRepository.countBySlaAssigneIsNotNullAndSlaResolutionRespecte(false);

        System.out.println("   Résolution OK: " + okResolution);
        System.out.println("   Résolution KO: " + koResolution);

        // ✅ Répartition par priorité (Déjà corrigé pour le type Number)
        Map<String, Double> repartitionPriorite = new HashMap<>();
        List<Object[]> resultatsParPriorite = ticketRepository.getTauxReussiteParPriorite();

        System.out.println("   Répartition par priorité:");
        for (Object[] row : resultatsParPriorite) {
            if (row[0] != null) {
                String priorite = row[0].toString();
                Double taux = 0.0;
                if (row[1] != null) {
                    taux = ((Number) row[1]).doubleValue();
                }
                repartitionPriorite.put(priorite, taux);
                System.out.println("      - " + priorite + ": " + String.format("%.2f", taux) + "%");
            }
        }

        return SlaStatisticsDTO.builder()
                .totalTicketsAvecSla(total)
                .slaGlobauxRespectes(respectes)
                .slaGlobauxDepasses(depasses)
                .tauxReussiteGlobal(Math.round(tauxGlobal * 100.0) / 100.0)
                .reussitePriseEnCharge(okPriseEnCharge)
                .echecPriseEnCharge(koPriseEnCharge)
                .reussiteResolution(okResolution)
                .echecResolution(koResolution)
                .tauxReussiteParPriorite(repartitionPriorite)
                .build();
    }
}
