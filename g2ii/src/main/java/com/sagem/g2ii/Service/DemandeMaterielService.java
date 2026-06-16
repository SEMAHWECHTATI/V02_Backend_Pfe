package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.ArticleDTO;
import com.sagem.g2ii.DTOs.DemandeMaterielDTO;
import com.sagem.g2ii.DTOs.DemandeReponseDTO;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Entity.Enumeration.TypeMateriel;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import com.sagem.g2ii.Entity.Inventaire.DemandeMateriel;
import com.sagem.g2ii.Repository.ConsommationPieceRepository;
import com.sagem.g2ii.Repository.DemandeMaterielRepository;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.IntUtilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandeMaterielService {
    private final DemandeMaterielRepository demandeRepository;
    private final ArticleRepository articleRepository;
    private final IntUtilisateur utilisateurRepository;
    private final ConsommationPieceRepository consommationRepository;
    private final AlerteService alerteService;


    /**
     * 7️⃣ Lister tous les demandes
     */
    public List<DemandeMateriel> listerTous() {
        return demandeRepository.findAll();
    }

    /**
     * 🔍 Récupérer une demande par son ID
     */
    public DemandeMaterielDTO getDemandeById(Long id) {
        log.info("🔍 Récupération de la demande ID: {}", id);
        DemandeMateriel demande = demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID: " + id));
        return convertToDTO(demande);
    }

    /**
     * 📝 Créer une demande de matériel + 🚨 Alerte Création Demande
     */
    @Transactional
    public DemandeMaterielDTO creerDemande(DemandeMaterielDTO dto, Long utilisateurId) {
        log.info("📝 Création demande matériel pour utilisateur: {}", utilisateurId);

        // Récupérer l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> {
                    log.error("❌ Utilisateur non trouvé: {}", utilisateurId);
                    return new RuntimeException("Utilisateur non trouvé");
                });

        // Récupérer l'article
        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> {
                    log.error("❌ Article non trouvé: {}", dto.getArticleId());
                    return new RuntimeException("Article non trouvé");
                });

        // Vérifier la quantité disponible
        if (article.getQuantiteEnStock() < dto.getQuantiteDemandee()) {
            log.warn("⚠️ Quantité insuffisante. Disponible: {}, Demandée: {}",
                    article.getQuantiteEnStock(), dto.getQuantiteDemandee());
            throw new RuntimeException(
                    "Quantité insuffisante en stock. Disponible: " + article.getQuantiteEnStock()
            );
        }

        // Créer la demande
        DemandeMateriel demande = DemandeMateriel.builder()
                .reference("DEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .article(article)
                .quantiteDemandee(dto.getQuantiteDemandee())
                .type(dto.getType())
                .statut(StatutDemande.EN_ATTENTE)
                .justification(dto.getJustification())
                .utilisateurDemandeur(utilisateur)
                .referenceTicket(dto.getReferenceTicket())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        DemandeMateriel saved = demandeRepository.save(demande);
        log.info("✅ Demande créée avec succès: {} (Référence: {})", saved.getId(), saved.getReference());

        // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Notification d'ouverture de flux
        try {
            String messageFlux = String.format(
                    "Nouvelle demande d'approvisionnement '%s' créée par %s %s pour l'article '%s' (Quantité: %d). En attente de validation gestionnaire.",
                    saved.getReference(),
                    utilisateur.getPrenom(),
                    utilisateur.getNom(),
                    article.getDesignation(),
                    saved.getQuantiteDemandee()
            );
            // Le moteur route vers les techniciens/gestionnaires associés au groupe de l'article
            alerteService.creerAlerte(article, messageFlux);
        } catch (Exception e) {
            log.error("⚠️ Échec d'émission de l'alerte à la création de la demande: {}", e.getMessage());
        }

        return convertToDTO(saved);
    }

    /**
     * ✅ Valider demande par Gestionnaire (AUCUNE CONSOMMATION)
     */
    @Transactional
    public DemandeMaterielDTO validerParGestionnaire(Long demandeId, Long gestionnaireId) {
        log.info("✅ Validation par gestionnaire - Demande: {}, Gestionnaire: {}", demandeId, gestionnaireId);

        // Récupérer la demande
        DemandeMateriel demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> {
                    log.error("❌ Demande non trouvée: {}", demandeId);
                    return new RuntimeException("Demande non trouvée");
                });

        // Vérifier le statut
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            log.error("❌ Statut incorrect. Statut actuel: {}", demande.getStatut());
            throw new RuntimeException("La demande n'est pas en attente. Statut actuel: " + demande.getStatut());
        }

        // Récupérer le gestionnaire
        Utilisateur gestionnaire = utilisateurRepository.findById(gestionnaireId)
                .orElseThrow(() -> {
                    log.error("❌ Gestionnaire non trouvé: {}", gestionnaireId);
                    return new RuntimeException("Gestionnaire non trouvé");
                });

        // Mettre à jour la demande
        demande.setStatut(StatutDemande.VALIDE_GESTIONNAIRE);
        demande.setUtilisateurGestionnaire(gestionnaire);
        demande.setDateValidationGestionnaire(LocalDateTime.now());
        demande.setDateModification(LocalDateTime.now());

        DemandeMateriel updated = demandeRepository.save(demande);
        log.info("✅ Demande validée par gestionnaire - Référence: {}", updated.getReference());

        return convertToDTO(updated);
    }

    /**
     * ✅ Valider demande par Admin (CONSOMME LE STOCK) + 🚨 Alerte Seuil de Stock Critique/Rupture
     */
    @Transactional
    public DemandeMaterielDTO validerParAdmin(Long demandeId, Long adminId) {
        log.info("✅ Validation par admin - Demande: {}, Admin: {}", demandeId, adminId);

        // Récupérer la demande
        DemandeMateriel demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> {
                    log.error("❌ Demande non trouvée: {}", demandeId);
                    return new RuntimeException("Demande non trouvée");
                });

        // Vérifier que la demande a été validée par le gestionnaire
        if (demande.getStatut() != StatutDemande.VALIDE_GESTIONNAIRE) {
            log.error("❌ La demande n'a pas été validée par le gestionnaire. Statut: {}", demande.getStatut());
            throw new RuntimeException(
                    "La demande doit être validée par le gestionnaire d'abord. Statut actuel: " + demande.getStatut()
            );
        }

        // Récupérer l'admin
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.error("❌ Admin non trouvé: {}", adminId);
                    return new RuntimeException("Admin non trouvé");
                });

        // 🔴 ÉTAPE CRITIQUE : Consommer le stock
        consommerStock(demande);

        // Mettre à jour la demande
        demande.setStatut(StatutDemande.CONSOMME);
        demande.setUtilisateurAdmin(admin);
        demande.setDateValidationAdmin(LocalDateTime.now());
        demande.setDateConsommation(LocalDateTime.now());
        demande.setDateModification(LocalDateTime.now());

        DemandeMateriel updated = demandeRepository.save(demande);
        log.info("✅ Demande validée par admin et stock consommé - Référence: {}", updated.getReference());

        // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Analyse et notification si franchissement de seuils critiques
        try {
            if (demande.getArticle() != null) {
                Article articleConcerne = demande.getArticle();

                // Si la quantité après déduction franchit le seuil d'alerte configuré
                if (articleConcerne.getQuantiteEnStock() <= articleConcerne.getSeuilMinimum()) {
                    String messageAlerte = String.format(
                            "Le stock de l'article '%s' (Réf: %s) vient d'être impacté par la validation de la demande %s. Quantité restante : %d unités.",
                            articleConcerne.getDesignation(),
                            articleConcerne.getReference(),
                            updated.getReference(),
                            articleConcerne.getQuantiteEnStock()
                    );

                    // AlerteService détermine automatiquement la sévérité (CRITIQUE si 0, HAUTE si sous seuil critique, etc.)
                    // et propage le signal multi-canal (WebSocket Angular + Email)
                    alerteService.creerAlerte(articleConcerne, messageAlerte);
                }
            }
        } catch (Exception e) {
            log.error("⚠️ Impossible de générer l'alerte de stock après validation: {}", e.getMessage());
        }

        return convertToDTO(updated);
    }

    /**
     * ❌ Rejeter une demande (AUCUNE CONSOMMATION) + 🚨 Alerte Rejet au Demandeur
     */
    @Transactional
    public DemandeMaterielDTO rejeterDemande(Long demandeId, Long validateurId, String motifRejet) {
        log.info("❌ Rejet demande: {} - Motif: {}", demandeId, motifRejet);

        // Récupérer la demande
        DemandeMateriel demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> {
                    log.error("❌ Demande non trouvée: {}", demandeId);
                    return new RuntimeException("Demande non trouvée");
                });

        // Vérifier que la demande peut être rejetée
        if (demande.getStatut() == StatutDemande.CONSOMME || demande.getStatut() == StatutDemande.REJETE) {
            log.error("❌ Impossible de rejeter cette demande. Statut: {}", demande.getStatut());
            throw new RuntimeException(
                    "Impossible de rejeter cette demande. Statut: " + demande.getStatut()
            );
        }

        // Mettre à jour la demande
        demande.setStatut(StatutDemande.REJETE);
        demande.setMotifRejet(motifRejet);
        demande.setDateModification(LocalDateTime.now());

        DemandeMateriel updated = demandeRepository.save(demande);
        log.info("✅ Demande rejetée - Référence: {}, Motif: {}", updated.getReference(), motifRejet);

        // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Notification immédiate de refus au demandeur
        try {
            if (demande.getUtilisateurDemandeur() != null && demande.getArticle() != null) {
                String messageRejet = String.format(
                        "Votre demande de matériel %s concernant l'article '%s' a été REJETÉE. Motif stipulé : %s",
                        updated.getReference(),
                        demande.getArticle().getDesignation(),
                        motifRejet
                );

                // Déclenche une alerte ciblée liée à cet article pour remonter l'anomalie
                alerteService.creerAlerte(demande.getArticle(), messageRejet);
            }
        } catch (Exception e) {
            log.error("⚠️ Échec d'émission de la notification de rejet de demande: {}", e.getMessage());
        }

        return convertToDTO(updated);
    }

    /**
     * 🔴 CONSOMMER LE STOCK - FONCTION CRITIQUE
     * Cette méthode est appelée UNIQUEMENT par validerParAdmin()
     */
    @Transactional
    private void consommerStock(DemandeMateriel demande) {
        log.info("🔴 Consommation stock - Article: {}, Quantité demandée: {}",
                demande.getArticle().getId(), demande.getQuantiteDemandee());

        Article article = demande.getArticle();
        Integer quantiteActuelle = article.getQuantiteEnStock();
        Integer nouvelleQuantite = quantiteActuelle - demande.getQuantiteDemandee();

        // Vérifier le stock disponible
        if (nouvelleQuantite < 0) {
            log.error("❌ Stock insuffisant. Actuel: {}, Demandé: {}",
                    quantiteActuelle, demande.getQuantiteDemandee());
            throw new RuntimeException(
                    "Stock insuffisant. Disponible: " + quantiteActuelle +
                            ", Demandé: " + demande.getQuantiteDemandee()
            );
        }

        // ✅ Réduire la quantité de l'article
        article.setQuantiteEnStock(nouvelleQuantite);
        article.setDateModification(LocalDateTime.now());
        Article articleMaj = articleRepository.save(article);
        log.info("📦 Stock article réduit: {} | {} → {}",
                article.getDesignation(), quantiteActuelle, nouvelleQuantite);

        // ✅ Enregistrer la consommation dans l'historique
        ConsommationPiece consommation = ConsommationPiece.builder()
                .article(article)
                .quantite(demande.getQuantiteDemandee())
                .commentaire("Consommation demande: " + demande.getReference())
                .responsable(demande.getUtilisateurDemandeur())
                .referenceTicket(demande.getReferenceTicket())
                .dateConsommation(LocalDateTime.now())
                .build();

        ConsommationPiece consomationSaved = consommationRepository.save(consommation);
        log.info("✅ Consommation enregistrée - ID: {}, Article: {}, Quantité: {}",
                consomationSaved.getId(), article.getDesignation(), demande.getQuantiteDemandee());
    }

    // --- Reste du service (Méthodes de listage et DTO conservées à l'identique) ---

    public List<DemandeMaterielDTO> getDemandesEnAttente() {
        log.info("📋 Récupération demandes en attente");
        List<DemandeMateriel> demandes = demandeRepository.findDemandesEnAttente();
        log.info("📋 Total: {} demandes en attente", demandes.size());
        return demandes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DemandeMaterielDTO> getDemandesValideeGestionnaire() {
        log.info("📋 Récupération demandes validées gestionnaire");
        List<DemandeMateriel> demandes = demandeRepository.findDemandesValideeGestionnaire();
        log.info("📋 Total: {} demandes validées gestionnaire", demandes.size());
        return demandes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DemandeMaterielDTO> getDemanduesUtilisateur(Long utilisateurId) {
        log.info("📋 Récupération demandes utilisateur: {}", utilisateurId);
        List<DemandeMateriel> demandes = demandeRepository.findByUtilisateurDemandeurId(utilisateurId);
        log.info("📋 Total: {} demandes pour utilisateur {}", demandes.size(), utilisateurId);
        return demandes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DemandeMaterielDTO convertToDTO(DemandeMateriel demande) {
        if (demande == null) return null;

        return DemandeMaterielDTO.builder()
                .id(demande.getId())
                .reference(demande.getReference())
                .quantiteDemandee(demande.getQuantiteDemandee())
                .type(demande.getType())
                .statut(demande.getStatut())
                .justification(demande.getJustification())
                .referenceTicket(demande.getReferenceTicket())
                .motifRejet(demande.getMotifRejet())
                .dateCreation(demande.getDateCreation())
                .dateModification(demande.getDateModification())
                .dateConsommation(demande.getDateConsommation())
                .dateValidationGestionnaire(demande.getDateValidationGestionnaire())
                .dateValidationAdmin(demande.getDateValidationAdmin())
                .articleId(demande.getArticle() != null ? demande.getArticle().getId() : null)
                .articleReference(demande.getArticle() != null ? demande.getArticle().getReference() : null)
                .articleDesignation(demande.getArticle() != null ? demande.getArticle().getDesignation() : "Article inconnu")
                .articleTypeArticle(demande.getArticle() != null && demande.getArticle().getTypeArticle() != null ? demande.getArticle().getTypeArticle().name() : "N/A")
                .articleCategorie(demande.getArticle() != null ? demande.getArticle().getCategorie() : "N/A")
                .articleQuantiteEnStock(demande.getArticle() != null ? demande.getArticle().getQuantiteEnStock() : 0)
                .articleSeuilMinimum(demande.getArticle() != null ? demande.getArticle().getSeuilMinimum() : 0)
                .articlePrixUnitaire(demande.getArticle() != null ? demande.getArticle().getPrixUnitaire() : java.math.BigDecimal.ZERO)
                .utilisateurDemandeur(
                        demande.getUtilisateurDemandeur() != null ?
                                DemandeReponseDTO.builder()
                                        .id(demande.getUtilisateurDemandeur().getId())
                                        .nom(demande.getUtilisateurDemandeur().getNom())
                                        .prenom(demande.getUtilisateurDemandeur().getPrenom())
                                        .email(demande.getUtilisateurDemandeur().getEmail())
                                        .telephone(demande.getUtilisateurDemandeur().getTelephone())
                                        .departement(demande.getUtilisateurDemandeur().getDepartement())
                                        .roleDemande(demande.getUtilisateurDemandeur().getRole())
                                        .build()
                                : null
                )
                .utilisateurGestionnaireId(demande.getUtilisateurGestionnaire() != null ? demande.getUtilisateurGestionnaire().getId() : null)
                .utilisateurGestionnaireNom(demande.getUtilisateurGestionnaire() != null ? demande.getUtilisateurGestionnaire().getNom() : null)
                .utilisateurGestionnairePrenom(demande.getUtilisateurGestionnaire() != null ? demande.getUtilisateurGestionnaire().getPrenom() : null)
                .utilisateurAdminId(demande.getUtilisateurAdmin() != null ? demande.getUtilisateurAdmin().getId() : null)
                .utilisateurAdminNom(demande.getUtilisateurAdmin() != null ? demande.getUtilisateurAdmin().getNom() : null)
                .utilisateurAdminPrenom(demande.getUtilisateurAdmin() != null ? demande.getUtilisateurAdmin().getPrenom() : null)
                .build();
    }
}