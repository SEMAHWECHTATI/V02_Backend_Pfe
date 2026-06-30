package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.ArticleDTO;
import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.ActionAudit;
import com.sagem.g2ii.Entity.Enumeration.ModuleAudit;
import com.sagem.g2ii.Entity.Enumeration.NiveauAudit;
import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Localisation;
import com.sagem.g2ii.Entity.Inventaire.Fournisseur;
import com.sagem.g2ii.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final LocalisationRepository localisationRepository;
    private final FournisseurRepository fournisseurRepository;
    private final AlerteService alerteService;
    private final JournalAuditService journalAuditService; // 🌟 Injection du service d'audit
    private final IntUtilisateur utilisateurRepository;
    private final IntGroupe groupeRepository;

    /**
     * Helper pour extraire l'utilisateur connecté depuis le SecurityContext de Spring Security
     */
    private Utilisateur getConnectedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Utilisateur) {
                return (Utilisateur) principal;
            }
        } catch (Exception e) {
            // Hors contexte session (ex: initialisation automatique au démarrage)
        }
        return null;
    }

    /**
     * ✅ Créer un nouvel article
     */
    public ArticleDTO creerArticle(ArticleDTO dto, Utilisateur operateur) {
        log.info("📝 Tentative de création de l'article: {} (Catégorie: {})", dto.getDesignation(), dto.getCategorie());

        if (dto.getCategorie() == null) {
            throw new IllegalArgumentException("La catégorie de l'article est obligatoire.");
        }
        if (dto.getReference() == null || dto.getReference().isBlank()) {
            throw new IllegalArgumentException("La référence de l'article est obligatoire.");
        }

        // 🌟 SÉCURITÉ & AUTOMATISATION
        Utilisateur auteurPhysique = null;
        Groupe groupeDuTechnicien = null;

        if (operateur != null && operateur.getId() != null) {
            auteurPhysique = utilisateurRepository.findById(operateur.getId()).orElse(null);

            // 🚀 Extraction automatique du groupe depuis la relation @ManyToMany
            if (auteurPhysique != null && auteurPhysique.getGroupes() != null && !auteurPhysique.getGroupes().isEmpty()) {

                // Stratégie optionnelle : Prendre de préférence un groupe qui n'est pas "Demandeur"
                groupeDuTechnicien = auteurPhysique.getGroupes().stream()
                        .filter(g -> g.getNomGroupes() != null && !g.getNomGroupes().name().equals("Demandeur"))
                        .findFirst()
                        // Si aucun groupe n'est filtré ou s'il n'y a qu'un groupe, on prend le premier de la liste
                        .orElse(auteurPhysique.getGroupes().get(0));

                if (groupeDuTechnicien != null) {
                    log.info("ℹ️ Groupe technique détecté automatiquement pour l'article : {} (ID: {})",
                            groupeDuTechnicien.getNomGroupes(), groupeDuTechnicien.getId());
                }
            }
        }

        Article article = Article.builder()
                .categorie(dto.getCategorie())
                .reference(dto.getReference().trim())
                .designation(dto.getDesignation())
                .description(dto.getDescription())
                .codeBarres(dto.getCodeBarres())
                .typeArticle(dto.getTypeArticle())
                .statut(dto.getStatut())
                .quantiteEnStock(dto.getQuantiteEnStock() != null ? dto.getQuantiteEnStock() : 0)
                .prixUnitaire(dto.getPrixUnitaire() != null ? dto.getPrixUnitaire() : BigDecimal.ZERO)
                .dateAchat(dto.getDateAchat())
                .dateGarantie(dto.getDateGarantie())
                .seuilMinimum(dto.getSeuilMinimum() != null ? dto.getSeuilMinimum() : 5)
                .seuilCritique(dto.getSeuilCritique() != null ? dto.getSeuilCritique() : 2)
                .creePar(auteurPhysique)
                .groupe(groupeDuTechnicien) // 🌟 Liaison automatique et sécurisée en BDD !
                .build();

        // Liaison de la Localisation
        if (dto.getLocalisationId() != null) {
            Localisation loc = localisationRepository.findById(dto.getLocalisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Localisation introuvable avec l'ID: " + dto.getLocalisationId()));
            article.setLocalisation(loc);
        }

        // Liaison du Fournisseur
        if (dto.getFournisseurId() != null) {
            Fournisseur f = fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable avec l'ID: " + dto.getFournisseurId()));
            article.setFournisseur(f);
        }

        Article saved = articleRepository.save(article);
        log.info("✅ Article enregistré avec succès en BD. ID: {}, affecté au Groupe ID: {}",
                saved.getId(), saved.getGroupe() != null ? saved.getGroupe().getId() : "Aucun");

        // Journal d'audit
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.CREATE_MATERIEL,
                "Article",
                saved.getId(),
                String.format("Création de l'article '%s' (Réf: %s) dans le catalogue.", saved.getDesignation(), saved.getReference()),
                null,
                String.format("{ref: '%s', designation: '%s', qte: %d}", saved.getReference(), saved.getDesignation(), saved.getQuantiteEnStock()),
                NiveauAudit.INFO,
                true,
                auteurPhysique
        );

        try {
            verifierAlertes(saved);
        } catch (Exception e) {
            log.error("⚠️ Impossible de vérifier les alertes pour l'article ID {}: {}", saved.getId(), e.getMessage());
        }

        return convertToDTO(saved);
    }    /**
     * ✅ Modifier un article
     */
    public ArticleDTO modifierArticle(Long id, ArticleDTO dto) {
        log.info("✏️ Modification article ID: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'ID: " + id));

        // Capture de l'état initial avant modifications
        String ancienneValeurStr = String.format("{designation: '%s', type: '%s', statut: '%s', pu: %s}",
                article.getDesignation(), article.getTypeArticle(), article.getStatut(), article.getPrixUnitaire());

        article.setCategorie(dto.getCategorie());
        article.setDesignation(dto.getDesignation());
        article.setDescription(dto.getDescription());
        article.setTypeArticle(dto.getTypeArticle());
        article.setStatut(dto.getStatut());
        article.setPrixUnitaire(dto.getPrixUnitaire());

        if (dto.getLocalisationId() != null) {
            article.setLocalisation(localisationRepository.findById(dto.getLocalisationId()).orElse(null));
        } else {
            article.setLocalisation(null);
        }

        if (dto.getFournisseurId() != null) {
            article.setFournisseur(fournisseurRepository.findById(dto.getFournisseurId()).orElse(null));
        } else {
            article.setFournisseur(null);
        }

        Utilisateur modificateur = getConnectedUser();
        if (modificateur != null && modificateur.getGroupes() != null && !modificateur.getGroupes().isEmpty()) {
            Groupe groupeModif = modificateur.getGroupes().stream()
                    .filter(g -> g.getNomGroupes() != null && !g.getNomGroupes().name().equals("Demandeur"))
                    .findFirst()
                    .orElse(modificateur.getGroupes().get(0));
            article.setGroupe(groupeModif);
        }

        Article updated = articleRepository.save(article);

        String nouvelleValeurStr = String.format("{designation: '%s', type: '%s', statut: '%s', pu: %s}",
                updated.getDesignation(), updated.getTypeArticle(), updated.getStatut(), updated.getPrixUnitaire());

        // 🌟 LOG D'AUDIT : Modification de la fiche article
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.UPDATE_MATERIEL, // Changement générique d'attributs
                "Article",
                updated.getId(),
                String.format("Modification des propriétés de la fiche article de '%s' (Réf: %s).", updated.getDesignation(), updated.getReference()),
                ancienneValeurStr,
                nouvelleValeurStr,
                NiveauAudit.INFO,
                true,
                getConnectedUser()
        );

        verifierAlertes(updated);
        return convertToDTO(updated);
    }

    /**
     * 🗑️ Archiver un article (Suppression logique)
     */
    public void archiveArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));

        String statutPrecedent = String.valueOf(article.getStatut());
        article.setStatut(StatutArticle.ARCHIVÉ);
        Article saved = articleRepository.save(article);
        log.info("🗑️ Article archivé ID: {}", id);

        // 🌟 LOG D'AUDIT : Archivage catalogue
        journalAuditService.enregistrerLogAvance(
                ModuleAudit.STOCK,
                ActionAudit.DELETE_STOCK, // Passage en statut ARCHIVE / BLOCAGE
                "Article",
                id,
                String.format("Archivage logique de la référence article '%s' dans le catalogue.", saved.getDesignation()),
                String.format("{statut: '%s'}", statutPrecedent),
                String.format("{statut: '%s'}", StatutArticle.ARCHIVÉ),
                NiveauAudit.WARNING,
                true,
                getConnectedUser()
        );
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(Long id) {
        return articleRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleByReference(String reference) {
        return articleRepository.findByReference(reference)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec la référence: " + reference));
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleByCodeBarres(String codeBarres) {
        return articleRepository.findByCodeBarres(codeBarres)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec le code-barres: " + codeBarres));
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> getArticlesWithLowStock() {
        return articleRepository.findArticlesAvecStockFaible().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> getArticlesWithCriticalStock() {
        return articleRepository.findArticlesAvecStockCritique().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> searchArticles(String keyword) {
        return articleRepository.searchByKeyword(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void verifierAlertes(Article article) {
        if (article.getQuantiteEnStock() != null) {
            if (article.getQuantiteEnStock() <= article.getSeuilCritique()) {
                alerteService.creerAlerte(article, "Stock critique pour: " + article.getDesignation() + " " + article.getCategorie() + " " + article.getReference());
            } else if (article.getQuantiteEnStock() <= article.getSeuilMinimum()) {
                alerteService.creerAlerte(article, "Stock faible pour: " + article.getDesignation() + " " + article.getCategorie() + " " + article.getReference());
            }
        }
    }

    private ArticleDTO convertToDTO(Article article) {
        if (article == null) return null;

        return ArticleDTO.builder()
                .id(article.getId())
                .categorie(article.getCategorie())
                .reference(article.getReference())
                .designation(article.getDesignation())
                .description(article.getDescription())
                .codeBarres(article.getCodeBarres())
                .typeArticle(article.getTypeArticle())
                .statut(article.getStatut())
                .quantiteEnStock(article.getQuantiteEnStock())
                .prixUnitaire(article.getPrixUnitaire())
                .fournisseurId(article.getFournisseur() != null ? article.getFournisseur().getId() : null)
                .fournisseurNom(article.getFournisseur() != null ? article.getFournisseur().getNom() : "Aucun")

                // 🌟 AJOUT ICI : Permet de renvoyer le groupeId à Angular
                .groupeId(article.getGroupe() != null ? article.getGroupe().getId() : null)

                .seuilMinimum(article.getSeuilMinimum())
                .seuilCritique(article.getSeuilCritique())
                .dateAchat(article.getDateAchat())
                .dateGarantie(article.getDateGarantie())
                .localisationId(article.getLocalisation() != null ? article.getLocalisation().getId() : null)
                .localisationLabel(article.getLocalisation() != null ?
                        article.getLocalisation().getBatiment() + " - " + article.getLocalisation().getBureau() : "Non localisé")
                .dateCreation(article.getDateCreation())
                .dateModification(article.getDateModification())
                .valeurTotal(article.getValeurTotal())
                .build();
    }
    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue() {
        BigDecimal total = articleRepository.getTotalInventoryValue();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Integer getTotalQuantityInStock() {
        Integer total = articleRepository.getTotalQuantityInStock();
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public Long getTotalArticles() {
        return articleRepository.count();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getValueByType() {
        Map<String, BigDecimal> valueByType = new HashMap<>();
        for (TypeArticle type : TypeArticle.values()) {
            BigDecimal value = articleRepository.getTotalValueByType(type);
            valueByType.put(type.getLabel(), value != null ? value : BigDecimal.ZERO);
        }
        return valueByType;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getValueByStatut() {
        Map<String, BigDecimal> valueByStatut = new HashMap<>();
        for (StatutArticle statut : StatutArticle.values()) {
            BigDecimal value = articleRepository.getTotalValueByStatut(statut);
            valueByStatut.put(statut.getLabel(), value != null ? value : BigDecimal.ZERO);
        }
        return valueByStatut;
    }
}