package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.TicketCreationDTO;
import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.CategorieRepo;
import com.sagem.g2ii.Repository.IntGroupe;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Repository.TicketRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    private HistoriqueTicketService historiqueService;

    /**
     * 1️⃣ Créer un ticket depuis DTO
     */
    @Transactional
    public Ticket creerTicket(TicketCreationDTO dto) {
        System.out.println("📨 Création de ticket");
        System.out.println("   Titre: " + dto.getTitre());

        // ✅ VALIDATION
        if (dto.getTitre() == null || dto.getTitre().trim().isEmpty()) {
            throw new RuntimeException("Le titre est requis");
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new RuntimeException("La description est requise");
        }

        // ✅ RÉCUPÉRER LES ENTITÉS
        Utilisateur demandeur = utilisateurRepository.findById(dto.getDemandeurId())
                .orElseThrow(() -> new RuntimeException("Demandeur non trouvé"));

        Categorie categorie = categorieRepository.findById(dto.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

        Groupe groupe = groupeRepository.findById(dto.getGroupeId())
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

        System.out.println("✅ Entités récupérées");
        System.out.println("   Demandeur: " + demandeur.getEmail());
        System.out.println("   Catégorie: " + categorie.getNomCategorie());
        System.out.println("   Groupe: " + groupe.getNomGroupes());

        // ✅ CRÉER LE TICKET
        Ticket ticket = new Ticket();
        ticket.setTitre(dto.getTitre().trim());
        ticket.setDescription(dto.getDescription().trim());
        ticket.setPriorite(dto.getPriorite() != null ? dto.getPriorite() : Priorite.Moyenne);
        ticket.setStatut(StatutTicket.Nouveau);
        ticket.setDate(LocalDate.now());
        ticket.setDemandeur(demandeur);
        ticket.setCategorie(categorie);
        ticket.setGroupeAssigne(groupe);
        ticket.setSlaRespecte(true);

        // ✅ GÉNÉRER LA RÉFÉRENCE
        ticket.setReference(genererReference());

        System.out.println("📌 Référence générée: " + ticket.getReference());

        Ticket ticketSauvegarde = ticketRepository.save(ticket);

        System.out.println("✅ Ticket créé ID: " + ticketSauvegarde.getIdTicket());

        return ticketSauvegarde;
    }

    /**
     * 2️⃣ Générer une référence unique
     */
    private String genererReference() {
        String prefixeDate = "SAG" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyddMM"));

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

    /**
     * 3️⃣ Démarrer un ticket (NOUVEAU → EN_COURS)
     */
    @Transactional
    public Ticket demarrerTicket(Long idTicket, Long idUtilisateur) {
        System.out.println("▶️ Démarrage du ticket ID: " + idTicket);

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (ticket.getStatut() != StatutTicket.Nouveau) {
            throw new RuntimeException("Le ticket ne peut être démarré que depuis NOUVEAU");
        }

        StatutTicket ancienStatut = ticket.getStatut();
        ticket.setStatut(StatutTicket.En_Cours);
        ticket.setDatePriseEncharge(LocalDate.now());
        ticket.setTechnicienAssigne(utilisateur);

        Ticket ticketUpdated = ticketRepository.save(ticket);

        historiqueService.tracer(ticket, utilisateur, "Statut",
                ancienStatut.name(), StatutTicket.En_Cours.name());

        System.out.println("✅ Ticket démarré");

        return ticketUpdated;
    }

    /**
     * 4️⃣ Résoudre le ticket
     */
    @Transactional
    public Ticket resoudreTicket(Long idTicket, Long idUtilisateur, String noteResolution) {
        System.out.println("✅ Résolution du ticket ID: " + idTicket);

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (ticket.getStatut() != StatutTicket.En_Cours && ticket.getStatut() != StatutTicket.En_Attente) {
            throw new RuntimeException("Le ticket doit être EN_COURS ou EN_ATTENTE");
        }

        StatutTicket ancienStatut = ticket.getStatut();
        ticket.setStatut(StatutTicket.Resolu);
        ticket.setDateResolution(LocalDate.now());
        ticket.setNoteResolution(noteResolution);

        calculerSLA(ticket);

        Ticket ticketUpdated = ticketRepository.save(ticket);

        historiqueService.tracer(ticket, utilisateur, "Statut",
                ancienStatut.name(), StatutTicket.Resolu.name());

        System.out.println("✅ Ticket résolu. SLA respecté: " + ticket.getSlaRespecte());

        return ticketUpdated;
    }

    /**
     * 5️⃣ Clôturer le ticket
     */
    @Transactional
    public Ticket cloturerTicket(Long idTicket, Long idUtilisateur, String roleUtilisateur) {
        System.out.println("🔒 Clôture du ticket ID: " + idTicket);

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier permissions
        if (!roleUtilisateur.equalsIgnoreCase("ADMINISTRATEUR") &&
                !roleUtilisateur.equalsIgnoreCase("ADMIN") &&
                !ticket.getDemandeur().getId().equals(idUtilisateur)) {
            throw new RuntimeException("Seul l'admin ou le demandeur peut clôturer");
        }

        if (ticket.getStatut() != StatutTicket.Resolu) {
            throw new RuntimeException("Le ticket doit être RESOLU avant clôture");
        }

        StatutTicket ancienStatut = ticket.getStatut();
        ticket.setStatut(StatutTicket.Cloture);
        ticket.setDateCloture(LocalDate.now());

        Ticket ticketUpdated = ticketRepository.save(ticket);

        historiqueService.tracer(ticket, utilisateur, "Statut",
                ancienStatut.name(), StatutTicket.Cloture.name());

        System.out.println("✅ Ticket clôturé");

        return ticketUpdated;
    }

    /**
     * 6️⃣ Calculer le SLA
     */
    private void calculerSLA(Ticket ticket) {
        System.out.println("📊 Calcul du SLA");

        if (ticket.getDatePriseEncharge() != null && ticket.getDateResolution() != null) {
            long heures = ChronoUnit.HOURS.between(
                    ticket.getDatePriseEncharge().atStartOfDay(),
                    ticket.getDateResolution().atStartOfDay()
            );
            ticket.setDelaiResolution((double) heures);

            if (ticket.getCategorie() != null &&
                    ticket.getCategorie().getSlas() != null &&
                    !ticket.getCategorie().getSlas().isEmpty()) {

                int delaiMax = ticket.getCategorie().getSlas().get(0).getDelaiResolutionHeure();
                boolean respecte = heures <= delaiMax;
                ticket.setSlaRespecte(respecte);

                System.out.println("   Délai réel: " + heures + "h");
                System.out.println("   SLA max: " + delaiMax + "h");
                System.out.println("   Respecté: " + respecte);
            }
        }
    }

    /**
     * 7️⃣ Lister tous les tickets
     */
    public List<Ticket> listerTous() {
        return ticketRepository.findAll();
    }

    /**
     * 8️⃣ Lister par demandeur
     */
    public List<Ticket> listerParDemandeur(Long idDemandeur) {
        return ticketRepository.findByDemandeur_Id(idDemandeur);
    }

    /**
     * 9️⃣ Lister par groupe
     */
    public List<Ticket> listerParGroupe(Long idGroupe) {
        return ticketRepository.findByGroupeAssigne_Id(idGroupe);
    }

    /**
     * 🔟 Lister par statut
     */
    public List<Ticket> listerParStatut(StatutTicket statut) {
        return ticketRepository.findByStatut(statut);
    }

    /**
     * 1️⃣1️⃣ Récupérer par ID
     */
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));
    }

    /**
     * 1️⃣2️⃣ Supprimer un ticket
     */
    @Transactional
    public void supprimerTicket(Long id) {
        System.out.println("🗑️ Suppression du ticket ID: " + id);
        ticketRepository.deleteById(id);
        System.out.println("✅ Supprimé");
    }
}