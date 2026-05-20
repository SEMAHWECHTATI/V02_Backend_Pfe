package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.DemandeMaterielDTO;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Entity.Enumeration.TypeMateriel;
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
@Transactional
public class DemandeMaterielService {
    private final DemandeMaterielRepository demandeRepository;
    private final ArticleRepository articleRepository;
    private final IntUtilisateur utilisateurRepository;
    private final ConsommationPieceRepository consommationRepository;
    private final StockService stockService;

    /**
     * 📝 Créer une demande de matériel
     */
    public DemandeMaterielDTO creerDemande(DemandeMaterielDTO dto, Long utilisateurId) {
        log.info("📝 Création demande matériel");

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        // Vérifier la quantité disponible
        if (article.getQuantiteEnStock() < dto.getQuantiteDemandee()) {
            throw new RuntimeException("Quantité insuffisante en stock: " + article.getQuantiteEnStock());
        }

        DemandeMateriel demande = DemandeMateriel.builder()
                .reference("DEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .article(article)
                .quantiteDemandee(dto.getQuantiteDemandee())
                .type(dto.getType())
                .statut(StatutDemande.EN_ATTENTE)
                .justification(dto.getJustification())
                .utilisateurDemandeur(utilisateur)
                .referenceTicket(dto.getReferenceTicket())
                .build();

        DemandeMateriel saved = demandeRepository.save(demande);
        log.info("✅ Demande créée: {}", saved.getReference());

        return convertToDTO(saved);
    }

    /**
     * ✅ Valider demande par Gestionnaire
     */
    public DemandeMaterielDTO validerParGestionnaire(Long demandeId, Long gestionnaireId) {
        log.info("✅ Validation gestionnaire demande: {}", demandeId);

        DemandeMateriel demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new RuntimeException("La demande n'est pas en attente");
        }

        Utilisateur gestionnaire = utilisateurRepository.findById(gestionnaireId)
                .orElseThrow(() -> new RuntimeException("Gestionnaire non trouvé"));

        demande.setStatut(StatutDemande.VALIDE_GESTIONNAIRE);
        demande.setUtilisateurGestionnaire(gestionnaire);
        demande.setDateValidationGestionnaire(LocalDateTime.now());

        DemandeMateriel updated = demandeRepository.save(demande);
        log.info("✅ Demande validée gestionnaire");

        return convertToDTO(updated);
    }

    /**
     * ✅ Valider demande par Admin (CONSOMMATION AUTO)
     */
    public DemandeMaterielDTO validerParAdmin(Long demandeId, Long adminId) {
        log.info("✅ Validation admin demande: {}", demandeId);

        DemandeMateriel demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (demande.getStatut() != StatutDemande.VALIDE_GESTIONNAIRE) {
            throw new RuntimeException("La demande n'a pas été validée par le gestionnaire");
        }

        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        // ✅ ÉTAPE CRITIQUE : Consommer le stock
        consommerStock(demande);

        // Marquer comme consommé
        demande.setStatut(StatutDemande.CONSOMME);
        demande.setUtilisateurAdmin(admin);
        demande.setDateValidationAdmin(LocalDateTime.now());
        demande.setDateConsommation(LocalDateTime.now());

        DemandeMateriel updated = demandeRepository.save(demande);
        log.info("✅ Demande validée admin et stock consommé");

        return convertToDTO(updated);
    }

    /**
     * ❌ Rejeter une demande
     */
    public DemandeMaterielDTO rejeterDemande(Long demandeId, Long validateurId, String motifRejet) {
        log.info("❌ Rejet demande: {}", demandeId);

        DemandeMateriel demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (demande.getStatut() == StatutDemande.CONSOMME || demande.getStatut() == StatutDemande.REJETE) {
            throw new RuntimeException("Impossible de rejeter cette demande");
        }

        demande.setStatut(StatutDemande.REJETE);
        demande.setMotifRejet(motifRejet);

        DemandeMateriel updated = demandeRepository.save(demande);
        log.info("✅ Demande rejetée");

        return convertToDTO(updated);
    }

    /**
     * 🔴 CONSOMMER LE STOCK - FONCTION CRITIQUE
     */
    private void consommerStock(DemandeMateriel demande) {
        log.info("🔴 Consommation stock - Article: {}, Quantité: {}",
                demande.getArticle().getId(), demande.getQuantiteDemandee());

        Article article = demande.getArticle();
        Integer nouvelleQuantite = article.getQuantiteEnStock() - demande.getQuantiteDemandee();

        if (nouvelleQuantite < 0) {
            throw new RuntimeException("Stock insuffisant");
        }

        // ✅ Réduire la quantité de l'article
        article.setQuantiteEnStock(nouvelleQuantite);
        articleRepository.save(article);

        // ✅ Enregistrer la consommation dans l'historique
        ConsommationPiece consommation = ConsommationPiece.builder()
                .article(article)
                .quantite(demande.getQuantiteDemandee())
                .commentaire("Consommation demande: " + demande.getReference())
                .responsable(demande.getUtilisateurDemandeur())
                .referenceTicket(demande.getReferenceTicket())
                .build();

        consommationRepository.save(consommation);

        log.info("✅ Stock consommé: {} - {} passé de {} à {}",
                article.getDesignation(), demande.getQuantiteDemandee(),
                article.getQuantiteEnStock() + demande.getQuantiteDemandee(),
                nouvelleQuantite);
    }

    /**
     * 📋 Récupérer demandes en attente
     */
    public List<DemandeMaterielDTO> getDemandesEnAttente() {
        return demandeRepository.findDemandesEnAttente().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 📋 Récupérer demandes validées gestionnaire (en attente admin)
     */
    public List<DemandeMaterielDTO> getDemandesValideeGestionnaire() {
        return demandeRepository.findDemandesValideeGestionnaire().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 📋 Récupérer demandes par utilisateur
     */
    public List<DemandeMaterielDTO> getDemanduesUtilisateur(Long utilisateurId) {
        return demandeRepository.findByUtilisateurDemandeurId(utilisateurId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔄 Convertir Entity en DTO
     */
    private DemandeMaterielDTO convertToDTO(DemandeMateriel demande) {
        return DemandeMaterielDTO.builder()
                .id(demande.getId())
                .reference(demande.getReference())
                .articleId(demande.getArticle().getId())
                .articleReference(demande.getArticle().getReference())
                .articleDesignation(demande.getArticle().getDesignation())
                .quantiteDemandee(demande.getQuantiteDemandee())
                .type(demande.getType())
                .statut(demande.getStatut())
                .justification(demande.getJustification())
                .utilisateurDemandeurId(demande.getUtilisateurDemandeur().getId())
                .utilisateurDemandeurName(demande.getUtilisateurDemandeur().getPrenom() + " " +
                        demande.getUtilisateurDemandeur().getNom())
                .utilisateurGestionnaireId(demande.getUtilisateurGestionnaire() != null ?
                        demande.getUtilisateurGestionnaire().getId() : null)
                .utilisateurGestionnaireName(demande.getUtilisateurGestionnaire() != null ?
                        demande.getUtilisateurGestionnaire().getPrenom() + " " +
                                demande.getUtilisateurGestionnaire().getNom() : null)
                .dateValidationGestionnaire(demande.getDateValidationGestionnaire())
                .utilisateurAdminId(demande.getUtilisateurAdmin() != null ?
                        demande.getUtilisateurAdmin().getId() : null)
                .utilisateurAdminName(demande.getUtilisateurAdmin() != null ?
                        demande.getUtilisateurAdmin().getPrenom() + " " +
                                demande.getUtilisateurAdmin().getNom() : null)
                .dateValidationAdmin(demande.getDateValidationAdmin())
                .motifRejet(demande.getMotifRejet())
                .dateCreation(demande.getDateCreation())
                .dateModification(demande.getDateModification())
                .dateConsommation(demande.getDateConsommation())
                .referenceTicket(demande.getReferenceTicket())
                .build();
    }
}