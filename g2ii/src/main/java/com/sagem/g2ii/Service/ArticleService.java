package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.ArticleDTO;
import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Localisation;
import com.sagem.g2ii.Entity.Inventaire.Fournisseur;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.FournisseurRepository;
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
    private final FournisseurRepository fournisseurRepository;
    private final AlerteService alerteService;

    /**
     * ✅ Créer un nouvel article
     */
    public ArticleDTO creerArticle(ArticleDTO dto) {
        log.info("📝 Tentative de création de l'article: {} (Catégorie: {})", dto.getDesignation(), dto.getCategorie());

        if (dto.getCategorie() == null) {
            throw new IllegalArgumentException("La catégorie de l'article est obligatoire.");
        }
        if (dto.getReference() == null || dto.getReference().isBlank()) {
            throw new IllegalArgumentException("La référence de l'article est obligatoire.");
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
        log.info("✅ Article enregistré avec succès en BD. ID généré: {}", saved.getId());

        try {
            verifierAlertes(saved);
        } catch (Exception e) {
            log.error("⚠️ Impossible de vérifier les alertes pour l'article ID {}: {}", saved.getId(), e.getMessage());
        }

        return convertToDTO(saved);
    }

    /**
     * ✅ Modifier un article
     */
    public ArticleDTO modifierArticle(Long id, ArticleDTO dto) {
        log.info("✏️ Modification article ID: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'ID: " + id));

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

        Article updated = articleRepository.save(article);
        verifierAlertes(updated);

        return convertToDTO(updated);
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
                alerteService.creerAlerte(article, "Stock critique pour: " + article.getDesignation());
            } else if (article.getQuantiteEnStock() <= article.getSeuilMinimum()) {
                alerteService.creerAlerte(article, "Stock faible pour: " + article.getDesignation());
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

                // Extraction sécurisée des informations du Fournisseur
                .fournisseurId(article.getFournisseur() != null ? article.getFournisseur().getId() : null)
                .fournisseurNom(article.getFournisseur() != null ? article.getFournisseur().getNom() : "Aucun")

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

    public void archiveArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));
        article.setStatut(StatutArticle.ARCHIVÉ);
        articleRepository.save(article);
        log.info("🗑️ Article archivé ID: {}", id);
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
        return articleRepository.count(); // Utilise directement la méthode native optimisée de JPA
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