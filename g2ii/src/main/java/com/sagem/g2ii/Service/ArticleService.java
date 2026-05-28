package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.ArticleDTO;
import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Localisation;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.LocalisationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AlerteService alerteService;

    /**
     * ✅ Créer un nouvel article
     */
    public ArticleDTO creerArticle(ArticleDTO dto) {
        log.info("📝 Tentative de création de l'article: {} (Catégorie: {})", dto.getDesignation(), dto.getCategorie());

        // 1. Sécurité : Vérifier que la catégorie et la référence ne sont pas nulles avant d'envoyer à la BD
        if (dto.getCategorie() == null || dto.getCategorie().isBlank()) {
            throw new IllegalArgumentException("La catégorie de l'article est obligatoire.");
        }
        if (dto.getReference() == null || dto.getReference().isBlank()) {
            throw new IllegalArgumentException("La référence de l'article est obligatoire.");
        }

        // 2. Construction de l'entité
        Article article = Article.builder()
                .categorie(dto.getCategorie().toUpperCase().trim()) // Normalisation (ex: "ecrans" -> "ECRANS")
                .reference(dto.getReference().trim())
                .designation(dto.getDesignation())
                .description(dto.getDescription())
                .codeBarres(dto.getCodeBarres())
                .typeArticle(dto.getTypeArticle())
                .statut(dto.getStatut())
                .quantiteEnStock(dto.getQuantiteEnStock())
                .prixUnitaire(dto.getPrixUnitaire())
                .fournisseur(dto.getFournisseur())
                .dateAchat(dto.getDateAchat())
                .dateGarantie(dto.getDateGarantie())
                .seuilMinimum(dto.getSeuilMinimum())
                .seuilCritique(dto.getSeuilCritique())
                .build();

        // 3. Gestion propre de la localisation
        if (dto.getLocalisationId() != null) {
            Localisation loc = localisationRepository.findById(dto.getLocalisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Localisation introuvable avec l'ID: " + dto.getLocalisationId()));
            article.setLocalisation(loc);
        }

        // 4. Sauvegarde en Base de Données
        Article saved = articleRepository.save(article);
        log.info("✅ Article enregistré avec succès en BD. ID généré: {}", saved.getId());

        // 5. Vérification des alertes de stock (Seuils minimum / critique)
        try {
            verifierAlertes(saved);
        } catch (Exception e) {
            // On loggue l'erreur d'alerte sans bloquer la création de l'article principal
            log.error("⚠️ Impossible de vérifier les alertes pour l'article ID {}: {}", saved.getId(), e.getMessage());
        }

        return convertToDTO(saved);
    }

    /**
     * ✅ Modifier un article
     */
    public ArticleDTO modifierArticle(Long id, ArticleDTO dto) {
        log.info("✏️ Modification article: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));


        article.setCategorie(dto.getCategorie());
        article.setDesignation(dto.getDesignation());
        article.setDescription(dto.getDescription());
        article.setTypeArticle(dto.getTypeArticle());
        article.setStatut(dto.getStatut());
        article.setPrixUnitaire(dto.getPrixUnitaire());
        article.setFournisseur(dto.getFournisseur());
        article.setSeuilMinimum(dto.getSeuilMinimum());
        article.setSeuilCritique(dto.getSeuilCritique());

        if (dto.getLocalisationId() != null) {
            article.setLocalisation(localisationRepository.findById(dto.getLocalisationId()).orElse(null));
        }

        Article updated = articleRepository.save(article);
        verifierAlertes(updated);

        return convertToDTO(updated);
    }

    /**
     * ✅ Récupérer tous les articles
     */
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Récupérer article par ID
     */
    public ArticleDTO getArticleById(Long id) {
        return articleRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
    }

    /**
     * ✅ Rechercher par référence
     */
    public ArticleDTO getArticleByReference(String reference) {
        return articleRepository.findByReference(reference)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
    }

    /**
     * ✅ Rechercher par code-barres
     */
    public ArticleDTO getArticleByCodeBarres(String codeBarres) {
        return articleRepository.findByCodeBarres(codeBarres)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
    }

    /**
     * ✅ Articles avec stock faible
     */
    public List<ArticleDTO> getArticlesWithLowStock() {
        return articleRepository.findArticlesAvecStockFaible().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Articles avec stock critique
     */
    public List<ArticleDTO> getArticlesWithCriticalStock() {
        return articleRepository.findArticlesAvecStockCritique().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * ✅ Rechercher articles
     */
    public List<ArticleDTO> searchArticles(String keyword) {
        return articleRepository.searchByKeyword(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Vérifier et créer alertes
     */
    private void verifierAlertes(Article article) {
        if (article.getQuantiteEnStock() <= article.getSeuilCritique()) {
            alerteService.creerAlerte(article, "Stock critique pour: " + article.getDesignation());
        } else if (article.getQuantiteEnStock() <= article.getSeuilMinimum()) {
            alerteService.creerAlerte(article, "Stock faible pour: " + article.getDesignation());
        }
    }

    /**
     * 🔄 Convertir Entity en DTO
     */
    private ArticleDTO convertToDTO(Article article) {
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
                .prixUnitaire(article.getPrixUnitaire())  // ✅ Maintenant BigDecimal
                .fournisseur(article.getFournisseur())
                .seuilMinimum(article.getSeuilMinimum())
                .seuilCritique(article.getSeuilCritique())
                .localisationId(article.getLocalisation() != null ? article.getLocalisation().getId() : null)
                .localisationLabel(article.getLocalisation() != null ?
                        article.getLocalisation().getBatiment() + " - " + article.getLocalisation().getBureau() : null)
                .dateCreation(article.getDateCreation())
                .dateModification(article.getDateModification())
                .valeurTotal(article.getValeurTotal())  // ✅ Maintenant BigDecimal
                .build();
    }

    /**
     * 🗑️ Archiver un article
     */
    public void archiveArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        article.setStatut(StatutArticle.ARCHIVÉ);
        articleRepository.save(article);
        log.info("🗑️ Article archivé: {}", id);
    }

    /**
     /**
     * ✅ Valeur totale de l'inventaire
     */
    public BigDecimal getTotalInventoryValue() {
        log.info("💰 Calcul valeur totale inventaire");

        BigDecimal total = articleRepository.getTotalInventoryValue();

        if (total == null) {
            log.warn("⚠️ Valeur totale est null, retour 0");
            return BigDecimal.ZERO;
        }

        log.info("💰 Valeur totale inventaire: {} €", total);
        return total;
    }

    /**
     * ✅ Quantité totale en stock
     */
    public Integer getTotalQuantityInStock() {
        log.info("📊 Calcul quantité totale");
        Integer total = articleRepository.getTotalQuantityInStock();
        return total != null ? total : 0;
    }

    /**
     * ✅ Nombre total d'articles
     */
    public Long getTotalArticles() {
        log.info("📋 Calcul nombre total articles");
        return articleRepository.getTotalArticles();
    }

    /**
     * ✅ Valeur par type d'article
     */
    public Map<String, BigDecimal> getValueByType() {
        log.info("📊 Calcul valeur par type");

        Map<String, BigDecimal> valueByType = new HashMap<>();

        for (TypeArticle type : TypeArticle.values()) {
            BigDecimal value = articleRepository.getTotalValueByType(type);
            valueByType.put(type.getLabel(), value != null ? value : BigDecimal.ZERO);
        }

        return valueByType;
    }

    /**
     * ✅ Valeur par statut
     */
    public Map<String, BigDecimal> getValueByStatut() {
        log.info("📊 Calcul valeur par statut");

        Map<String, BigDecimal> valueByStatut = new HashMap<>();

        for (StatutArticle statut : StatutArticle.values()) {
            BigDecimal value = articleRepository.getTotalValueByStatut(statut);
            valueByStatut.put(statut.getLabel(), value != null ? value : BigDecimal.ZERO);
        }

        return valueByStatut;
    }

}