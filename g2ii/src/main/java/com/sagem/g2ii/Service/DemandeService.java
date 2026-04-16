package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.ApprobationDTO;
import com.sagem.g2ii.DTOs.DemandeCreationDTO;
import com.sagem.g2ii.DTOs.DemandeReponseDTO;
import com.sagem.g2ii.Entity.Authentification.*;
import com.sagem.g2ii.Entity.Email.EmailQueue;
import com.sagem.g2ii.Entity.Enumeration.*;
import com.sagem.g2ii.Repository.*;
import lombok.RequiredArgsConstructor;
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

    // 1. Créer une nouvelle demande (Utilise le DTO Entrant)
    public DemandeReponseDTO creerDemande(DemandeCreationDTO dto) {

        // CORRECTION : Le groupe est optionnel lors de la demande !
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

    // 3. APPROUVER une demande (CORRIGÉ AVEC LES RÈGLES RBAC)
    @Transactional
    public void approuverDemande(Long demandeId, ApprobationDTO approbation) {
        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        // --- VÉRIFICATION DES RÈGLES MÉTIER DE L'ADMINISTRATEUR ---
        Groupe groupeChoisi = null;
        roleUtilisateur roleAccorde = approbation.getRoleAccorde();

        // 1. On vérifie si Angular a envoyé un ID de groupe
        if (approbation.getGroupeId() != null) {
            groupeChoisi = groupeRepo.findById(approbation.getGroupeId())
                    .orElseThrow(() -> new RuntimeException("Le groupe sélectionné par l'administrateur n'existe pas."));
        }
        // 2. Si aucun groupe n'est envoyé, on vérifie si le rôle l'exigeait obligatoirement
        else if (roleAccorde == roleUtilisateur.Technicien || roleAccorde == roleUtilisateur.Gestionnaire_Stock) {
            throw new RuntimeException("Erreur : Un " + roleAccorde.name() + " doit obligatoirement être affecté à un groupe !");
        }

        // Mise à jour de la demande
        demande.setStatut(statutDemandeInscription.ACCEPTEE);
        demande.setDateTraitement(LocalDateTime.now());
        demandeRepo.save(demande);

        // Génération du mot de passe
        String motDePasseTempClair = UUID.randomUUID().toString().substring(0, 8);

        // Création de l'utilisateur avec les choix de l'Admin
        Utilisateur nouvelUtilisateur = Utilisateur.builder()
                .nom(demande.getNom())
                .prenom(demande.getPrenom())
                .email(demande.getEmail())
                .matricule(demande.getMatricule())
                .telephone(demande.getTelephone())
                .departement(demande.getDepartement())
                .role(roleAccorde) // Le rôle définitif
                .motDePasse(passwordEncoder.encode(motDePasseTempClair))
                .motDepassetemporaire(true)
                .dateExpmdpTemp(LocalDateTime.now().plusHours(24))
                .statut(statutUtilisateur.Actif)
                .date_Creation_Compte(LocalDateTime.now())
                .groupes(groupeChoisi != null ? new ArrayList<>(Collections.singletonList(groupeChoisi)) : new ArrayList<>())
                .build();

        utilisateurRepo.save(nouvelUtilisateur);

        try {
            // Tentative d'envoi immédiat
            emailService.envoyerEmailBienvenue(nouvelUtilisateur.getEmail(), nouvelUtilisateur.getPrenom(), motDePasseTempClair);
            System.out.println("✅ Email envoyé avec succès en direct !");
        } catch (Exception e) {
            // 🚨 SI CA ECHOUE : ON SAUVEGARDE DANS LA TABLE EMAIL_QUEUE
            System.err.println("⚠️ Échec envoi direct. Sauvegarde en base de données...");

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
//            emailAttente.setContenu("Bonjour " + nouvelUtilisateur.getPrenom() + ", votre mot de passe est : " + motDePasseTempClair);
            emailAttente.setEnvoye(false);
            emailAttente.setTentatives(0);
            emailAttente.setDernierErreur(e.getMessage());

            emailQueueRepo.save(emailAttente); // C'est cette ligne qui remplit votre tableau !
            System.out.println("📌 Email mis en file d'attente avec succès.");
        }
        // Audit mis à jour avec le rôle
        JournalAudit log = JournalAudit.builder()
                .action(ActionAudit.APPROBATION_DEMANDE)
                .description("Approbation de la demande ID: " + demandeId + " en tant que " + roleAccorde)
                .dateAction(LocalDateTime.now())
                .adresseIp("127.0.0.1")
                .build();
        auditRepo.save(log);

        PreferenceNotification prefs = PreferenceNotification.builder()
                .utilisateur(nouvelUtilisateur)
                .canalEmail(true)
                .canalInApp(true)
                .actif(true)
                .typeAlerte(TypeAlerte.INSCRIPTION)
                .build();
        preferenceRepo.save(prefs);
    }

    // 4. REFUSER une demande
    public void refuserDemande(Long demandeId, String motif) {
        DemandeInscription demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        demande.setStatut(statutDemandeInscription.REFUSEE);
        demande.setMotifRefus(motif);
        demande.setDateTraitement(LocalDateTime.now());

        demandeRepo.save(demande);


        try {
            emailService.envoyerEmailRefus(demande.getEmail(), demande.getPrenom(), motif);
        } catch (Exception e) {
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
            emailQueueRepo.save(emailAttente);
        }
    }

    public void deletedemandeInscrip   (Long demandeId) {
        demandeRepo.deleteById(demandeId);
    }

    public List<DemandeInscription> getDemandesActives() {
        return demandeRepo.findByArchiveeFalse();
    }

    // 2. Obtenir les demandes archivées
    public List<DemandeInscription> getDemandesArchivees() {
        return demandeRepo.findByArchiveeTrue();
    }

    // 3. Archiver une demande
    public DemandeInscription archiverDemande(Long id) {
        DemandeInscription demande = demandeRepo.findById(id).orElseThrow();

        demande.setArchivee(true); // On change le statut !
        return demandeRepo.save(demande); // On sauvegarde

    }
}