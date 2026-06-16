package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.ApprobationDTO;
import com.sagem.g2ii.DTOs.DemandeCreationDTO;
import com.sagem.g2ii.DTOs.DemandeReponseDTO;
import com.sagem.g2ii.Entity.Authentification.*;
import com.sagem.g2ii.Entity.Email.EmailQueue;
import com.sagem.g2ii.Entity.Enumeration.*;
import com.sagem.g2ii.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DemandeService {

    @Autowired
    private IntdemandeInscri demandeRepo;
    @Autowired
    private IntUtilisateur utilisateurRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IntJournalAudit auditRepo;
    @Autowired
    private IntReferenceNotif preferenceRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private IntGroupe groupeRepo;
    @Autowired
    private EmailQueueRepository emailQueueRepo;
    @Autowired
    private AlerteService alerteService; // 🛠️ Injection du moteur d'alertes (Tâche 5)

    /**
     * 📝 1. Créer une nouvelle demande d'inscription + 🚨 Alerte Système
     */
    public DemandeReponseDTO creerDemande(DemandeCreationDTO dto) {
        log.info("📝 Réception d'une nouvelle demande d'inscription pour l'email: {}", dto.getEmail());

        // Le groupe est optionnel lors de la demande
        Groupe groupe = null;
        if (dto.getGroupeId() != null) {
            groupe = groupeRepo.findById(dto.getGroupeId())
                    .orElseThrow(() -> new RuntimeException("Groupe introuvable"));
        }

        DemandeInscription demande = new DemandeInscription();
        demande.setNom(dto.getNom());
        demande.setPrenom(dto.getPrenom());
        demande.setEmail(dto.getEmail());
        demande.setMatricule(dto.getMatricule());
        demande.setTelephone(dto.getTelephone());
        demande.setDepartement(dto.getDepartement());
        demande.setMotifDemande(dto.getMotifDemande());
        demande.setRoleDemande(dto.getRoleDemande());
        demande.setGroupeARejoindre(groupe);

        demande.setStatut(statutDemandeInscription.En_Attente);
        demande.setDateDemande(LocalDateTime.now());

        DemandeInscription demandeSauvegardee = demandeRepo.save(demande);
        log.info("✅ Demande d'inscription enregistrée en BDD [ID: {}]", demandeSauvegardee.getId());

        // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Notification aux Administrateurs sur l'interface Web
        try {
            String messageAlerte = String.format(
                    "Nouvelle demande d'inscription en attente : %s %s (%s) sollicite le rôle %s.",
                    demandeSauvegardee.getPrenom(),
                    demandeSauvegardee.getNom(),
                    demandeSauvegardee.getDepartement(),
                    demandeSauvegardee.getRoleDemande()
            );

            // On déclenche la création d'une alerte (Le service va notifier dynamiquement les Admins via WebSocket)
            alerteService.creerAlerte(null, messageAlerte);
        } catch (Exception e) {
            log.error("⚠️ Impossible d'émettre la notification de demande d'inscription: {}", e.getMessage());
        }

        return convertirEnDTO(demandeSauvegardee);
    }


    // 2. Récupérer toutes les demandes
    public List<DemandeReponseDTO> getAllDemandes() {
        List<DemandeInscription> demandes = demandeRepo.findAll();
        List<DemandeReponseDTO> reponseList = new ArrayList<>();

        for (DemandeInscription d : demandes) {
            reponseList.add(convertirEnDTO(d));
        }
        return reponseList;
    }

    private DemandeReponseDTO convertirEnDTO(DemandeInscription demande) {
        return DemandeReponseDTO.builder()
                .id(demande.getId())
                .nom(demande.getNom())
                .prenom(demande.getPrenom())
                .email(demande.getEmail())
                .motifDemande(demande.getMotifDemande())
                .departement(demande.getDepartement())
                .roleDemande(demande.getRoleDemande())
                .statut(demande.getStatut())
                .dateDemande(demande.getDateDemande())
                .nomGroupe(demande.getGroupeARejoindre() != null ? demande.getGroupeARejoindre().getNomGroupes() : null)
                .build();
    }

    /**
     * 🎉 3. APPROUVER une demande + Compte Utilisateur + 📨 Escalade Mail & Push
     */
    @Transactional
    public void approuverDemande(Long demandeId, ApprobationDTO approbation) {
        log.info("🎯 Approbation de la demande ID: {} par l'administrateur.", demandeId);

        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        // --- VÉRIFICATION DES RÈGLES MÉTIER DE L'ADMINISTRATEUR ---
        Groupe groupeChoisi = null;
        roleUtilisateur roleAccorde = approbation.getRoleAccorde();

        if (approbation.getGroupeId() != null) {
            groupeChoisi = groupeRepo.findById(approbation.getGroupeId())
                    .orElseThrow(() -> new RuntimeException("Le groupe sélectionné par l'administrateur n'existe pas."));
        }
        else if (roleAccorde == roleUtilisateur.Technicien || roleAccorde == roleUtilisateur.Gestionnaire_Stock) {
            throw new RuntimeException("Erreur : Un " + roleAccorde.name() + " doit obligatoirement être affecté à un groupe !");
        }

        // Mise à jour de la demande
        demande.setStatut(statutDemandeInscription.ACCEPTEE);
        demande.setDateTraitement(LocalDateTime.now());
        demandeRepo.save(demande);

        // Génération du mot de passe temporaire
        String motDePasseTempClair = UUID.randomUUID().toString().substring(0, 8);

        // Création de l'utilisateur
        Utilisateur nouvelUtilisateur = Utilisateur.builder()
                .nom(demande.getNom())
                .prenom(demande.getPrenom())
                .email(demande.getEmail())
                .matricule(demande.getMatricule())
                .telephone(demande.getTelephone())
                .departement(demande.getDepartement())
                .role(roleAccorde)
                .motDePasse(passwordEncoder.encode(motDePasseTempClair))
                .motDepassetemporaire(true)
                .dateExpmdpTemp(LocalDateTime.now().plusHours(24))
                .statut(statutUtilisateur.Actif)
                .date_Creation_Compte(LocalDateTime.now())
                .groupes(groupeChoisi != null ? new ArrayList<>(Collections.singletonList(groupeChoisi)) : new ArrayList<>())
                .build();

        utilisateurRepo.save(nouvelUtilisateur);

        // --- DISTRIBUTION NOTIFICATION (EMAIL) ---
        try {
            emailService.envoyerEmailBienvenue(nouvelUtilisateur.getEmail(), nouvelUtilisateur.getPrenom(), motDePasseTempClair);
            log.info("✅ Email de bienvenue expédié en direct à {}", nouvelUtilisateur.getEmail());
        } catch (Exception e) {
            log.warn("⚠️ Échec de l'envoi direct du mail. Transfert vers la file d'attente Outbox 'EmailQueue'...");

            EmailQueue emailAttente = new EmailQueue();
            emailAttente.setDestinataire(nouvelUtilisateur.getEmail());
            emailAttente.setSujet("Bienvenue ! Votre compte a été approuvé 🎉");
            emailAttente.setContenu("Bonjour " +  nouvelUtilisateur.getPrenom() + ",\n\n"
                    + "Votre demande d'inscription a été acceptée avec succès.\n\n"
                    + "Voici vos identifiants pour vous connecter à l'application :\n"
                    + "-------------------------------------------------\n"
                    + "👤 Login (Email) : " + nouvelUtilisateur.getEmail() + "\n"
                    + "🔑 Mot de passe  : " +  motDePasseTempClair + "\n"
                    + "-------------------------------------------------\n\n"
                    + "⚠️ Veuillez vous connecter et changer ce mot de passe immédiatement lors de votre première connexion.\n\n"
                    + "Cordialement,\n"
                    + "L'équipe Support IT");
            emailAttente.setEnvoye(false);
            emailAttente.setTentatives(0);
            emailAttente.setDernierErreur(e.getMessage());

            emailQueueRepo.save(emailAttente);
            log.info("📌 Email de bienvenue consigné dans EmailQueue avec succès.");
        }

        // 🚨 PUSH NOTIFICATION (Tâche 5) : Signaler au système l'intégration du nouveau profil
        try {
            String messagePush = String.format("Le profil de %s %s a été approuvé avec succès en tant que %s.",
                    nouvelUtilisateur.getPrenom(), nouvelUtilisateur.getNom(), roleAccorde.name());
            alerteService.creerAlerte(null, messagePush);
        } catch (Exception e) {
            log.error("⚠️ Échec d'émission de l'alerte push d'approbation: {}", e.getMessage());
        }

        // Journal d'audit
        JournalAudit logAudit = JournalAudit.builder()
                .action(ActionAudit.APPROBATION_DEMANDE)
                .description("Approbation de la demande ID: " + demandeId + " en tant que " + roleAccorde)
                .dateAction(LocalDateTime.now())
                .adresseIp("127.0.0.1")
                .build();
        auditRepo.save(logAudit);

        // Profil des préférences par défaut (Canal Web + Email Actifs)
        PreferenceNotification prefs = PreferenceNotification.builder()
                .utilisateur(nouvelUtilisateur)
                .canalEmail(true)
                .canalInApp(true)
                .actif(true)
                .typeAlerte(TypeAlerte.INSCRIPTION)
                .build();
        preferenceRepo.save(prefs);
    }

    /**
     * ❌ 4. REFUSER une demande + 📨 Routage Mail d'information
     */
    @Transactional
    public void refuserDemande(Long demandeId, String motif) {
        log.info("❌ Rejet de la demande d'inscription ID: {} | Motif: {}", demandeId, motif);

        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        demande.setStatut(statutDemandeInscription.REFUSEE);
        demande.setMotifRefus(motif);
        demande.setDateTraitement(LocalDateTime.now());

        demandeRepo.save(demande);

        try {
            emailService.envoyerEmailRefus(demande.getEmail(), demande.getPrenom(), motif);
            log.info("✅ Email de refus envoyé directement à {}", demande.getEmail());
        } catch (Exception e) {
            log.warn("⚠️ Échec d'envoi postal direct du refus. Enregistrement dans EmailQueue.");
            EmailQueue emailAttente = new EmailQueue();
            emailAttente.setDestinataire(demande.getEmail());
            emailAttente.setSujet("Mise à jour de votre demande d'inscription");
            emailAttente.setContenu("Bonjour " + demande.getPrenom() + ",\n\n"
                    + "Nous vous informons que votre demande d'inscription n'a malheureusement pas pu être acceptée.\n\n"
                    + "Motif du refus : " + motif + "\n\n"
                    + "Si vous pensez qu'il s'agit d'une erreur, veuillez contacter le support.\n\n"
                    + "Cordialement,\n"
                    + "L'équipe Support IT");
            emailAttente.setEnvoye(false);
            emailAttente.setTentatives(0);
            emailQueueRepo.save(emailAttente);
        }
    }

    public void deletedemandeInscrip(Long demandeId) {
        demandeRepo.deleteById(demandeId);
    }

    public List<DemandeInscription> getDemandesActives() {
        return demandeRepo.findByArchiveeFalse();
    }

    public List<DemandeInscription> getDemandesArchivees() {
        return demandeRepo.findByArchiveeTrue();
    }

    public DemandeInscription archiverDemande(Long id) {
        DemandeInscription demande = demandeRepo.findById(id).orElseThrow();
        demande.setArchivee(true);
        return demandeRepo.save(demande);
    }
}