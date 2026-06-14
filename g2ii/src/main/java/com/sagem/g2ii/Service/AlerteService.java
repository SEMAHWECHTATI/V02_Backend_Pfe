package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.AlerteDTO;
import com.sagem.g2ii.Entity.Enumeration.Severite;
import com.sagem.g2ii.Entity.Enumeration.StatutAlerte;
import com.sagem.g2ii.Entity.Enumeration.TypeAlerte;
import com.sagem.g2ii.Entity.Inventaire.Alerte;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Repository.AlerteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlerteService {

    private final AlerteRepository alerteRepository;

    /**
     * ✅ Créer une alerte sécurisée
     */
    public AlerteDTO creerAlerte(Article article, String message) {
        log.info("⚠️ Création alerte: {}", message);

        TypeAlerte type;
        Severite severite;

        if (article.getQuantiteEnStock() == 0) {
            type = TypeAlerte.RUPTURE_STOCK;
            severite = Severite.CRITIQUE;
        } else if (article.getQuantiteEnStock() <= article.getSeuilCritique()) {
            type = TypeAlerte.SEUIL_CRITIQUE;
            severite = Severite.HAUTE;
        } else {
            type = TypeAlerte.SEUIL_MINIMUM;
            severite = Severite.MOYENNE;
        }

        Alerte alerte = Alerte.builder()
                .type(type)
                .message(message)
                .severite(severite)
                .statut(StatutAlerte.NOUVELLE)
                .build();

        // 💡 Utilisation de votre méthode Helper (garantit l'absence de NullPointerException)
        alerte.addArticle(article);

        Alerte saved = alerteRepository.save(alerte);
        log.info("✅ Alerte créée avec succès, ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    public void marquerCommeLue(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
        alerte.setStatut(StatutAlerte.LUE);
        alerteRepository.save(alerte);
        log.info("👁️ Alerte marquée comme lue: {}", alerteId);
    }

    public void marquerCommeTraitee(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
        alerte.setStatut(StatutAlerte.TRAITEE);
        alerteRepository.save(alerte);
        log.info("✅ Alerte marquée comme traitée: {}", alerteId);
    }

    public List<AlerteDTO> getAlerteNonTraitees() {
        return alerteRepository.findByStatut(StatutAlerte.NOUVELLE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AlerteDTO> getAlerteCritique() {
        return alerteRepository.findByStatutAndSeveriteOrderByDateCreationDesc(
                        StatutAlerte.NOUVELLE, Severite.CRITIQUE
                ).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔄 Convertir Entity en DTO (Corrigé et Sécurisé 🛠️)
     */
    private AlerteDTO convertToDTO(Alerte alerte) {
        // 💡 Utilisation systématique de la liste isolée pour éviter les mauvaises surprises
        List<Article> articlesAssocies = alerte.getArticles() != null ? alerte.getArticles() : new ArrayList<>();

        return AlerteDTO.builder()
                .id(alerte.getId())
                .type(alerte.getType())
                .message(alerte.getMessage())
                .severite(alerte.getSeverite())
                .statut(alerte.getStatut())
                .dateCreation(alerte.getDateCreation())
                .dateAcquittement(alerte.getDateAcquittement())
                // 💡 Remplacement de alerte.getArticles() par articlesAssocies
                .articleIds(articlesAssocies.stream().map(Article::getId).collect(Collectors.toList()))
                .articleDesignations(articlesAssocies.stream().map(Article::getDesignation).collect(Collectors.toList()))
                .build();
    }

    public void supprimerAlerte(Long alerteId) {
        log.info("🗑️ Suppression alerte: {}", alerteId);
        alerteRepository.deleteById(alerteId);
    }
}