package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.EquipementRequestDto;
import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
import com.sagem.g2ii.Entity.Inventaire.Article;
import com.sagem.g2ii.Entity.Inventaire.Equipement;
import com.sagem.g2ii.Entity.Inventaire.Localisation;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.EquipementRepository;
import com.sagem.g2ii.Repository.LocalisationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetailleEquipementService {

    private final EquipementRepository equipementRepository;
    private final LocalisationRepository localisationRepository;
    private final ArticleRepository articleRepository;
    private final AlerteService alerteService; // 🛠️ Injection du service d'alerte (Tâche 5)

    public List<Equipement> listerTous() {
        return equipementRepository.findAll();
    }

    public long compterTotal() {
        return equipementRepository.count();
    }

    public long compterParStatut(StatutArticle statut) {
        return equipementRepository.countByStatut(statut);
    }

    /**
     * Récupère les détails complets d'un équipement par son ID
     */
    @Transactional(readOnly = true)
    public Equipement obtenirDetailsEquipement(Long id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Équipement introuvable avec l'ID : " + id));
    }

    /**
     * Récupère les détails d'un équipement via son code-barres (utile pour les scans)
     */
    @Transactional(readOnly = true)
    public Equipement obtenirParCodeBarres(String codeBarres) {
        return equipementRepository.findByCodeBarres(codeBarres)
                .orElseThrow(() -> new EntityNotFoundException("Équipement introuvable avec le code-barres : " + codeBarres));
    }

    /**
     * Met à jour le statut et/ou la localisation physique d'un équipement + 🚨 Alerte Changement/Rebut
     */
    @Transactional
    public Equipement mettreAJourStatutEtLocalisation(Long equipementId, StatutArticle nouveauStatut, Long nouvelleLocalisationId) {
        log.info("🔄 Mise à jour équipement ID: {} - Statut réclamé: {}", equipementId, nouveauStatut);

        Equipement equipement = obtenirDetailsEquipement(equipementId);
        StatutArticle ancienStatut = equipement.getStatut();
        Localisation ancienneLocalisation = equipement.getLocalisation();

        // Mise à jour du statut
        if (nouveauStatut != null && nouveauStatut != ancienStatut) {
            equipement.setStatut(nouveauStatut);

            // Logique métier : Si mis au rebut, on enregistre la date
            if (nouveauStatut == StatutArticle.A_RECYCLER) {
                equipement.setDateMiseAuRebut(LocalDateTime.now());
            }
        }

        // Mise à jour de la localisation physique si demandée
        if (nouvelleLocalisationId != null && (ancienneLocalisation == null || !ancienneLocalisation.getId().equals(nouvelleLocalisationId))) {
            Localisation nouvelleLocalisation = localisationRepository.findById(nouvelleLocalisationId)
                    .orElseThrow(() -> new EntityNotFoundException("Localisation introuvable avec l'ID : " + nouvelleLocalisationId));
            equipement.setLocalisation(nouvelleLocalisation);
        }

        Equipement equipementMisAJour = equipementRepository.save(equipement);
        log.info("✅ Équipement mis à jour avec succès : {}", equipementMisAJour.getNumeroSerie());

        // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Détection de mise au rebut ou de transfert critique
        try {
            if (nouveauStatut == StatutArticle.A_RECYCLER && ancienStatut != StatutArticle.A_RECYCLER) {
                String messageRebut = String.format(
                        "⚠️ MATÉRIEL DÉFECTUEUX / HORS SERVICE : L'équipement '%s' (N° Série: %s, Modèle: %s) a été officiellement mis au rebut / à recycler.",
                        equipementMisAJour.getDesignation(),
                        equipementMisAJour.getNumeroSerie(),
                        equipementMisAJour.getArticle().getDesignation()
                );
                // Notification multi-canal : Routage via l'article pour identifier les techniciens/gestionnaires affectés au groupe de cette pièce
                alerteService.creerAlerte(equipementMisAJour.getArticle(), messageRebut);
            }
            else if (nouveauStatut != null && nouveauStatut != ancienStatut) {
                String messageStatut = String.format(
                        "🔄 Changement d'état : L'équipement '%s' (%s) est passé du statut %s au statut %s.",
                        equipementMisAJour.getDesignation(),
                        equipementMisAJour.getNumeroSerie(),
                        ancienStatut != null ? ancienStatut.name() : "INCONNU",
                        nouveauStatut.name()
                );
                alerteService.creerAlerte(equipementMisAJour.getArticle(), messageStatut);
            }
        } catch (Exception e) {
            log.error("⚠️ Impossible d'émettre la notification de mise à jour de l'équipement: {}", e.getMessage());
        }

        return equipementMisAJour;
    }

    @Transactional(readOnly = true)
    public List<Equipement> listerParStatut(StatutArticle statut) {
        return equipementRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<Equipement> listerParLocalisation(Long localisationId) {
        return equipementRepository.findByLocalisationId(localisationId);
    }

    /**
     * Créer un équipement (Génération SFC-XXXXXXXX) + 🚨 Notification Push
     */
    @Transactional
    public Equipement creerEquipement(EquipementRequestDto dto) {
        log.info("➕ Création d'une nouvelle unité d'équipement pour l'article ID: {}", dto.getArticleId());

        // 1. Récupération de l'article (Obligatoire)
        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec l'ID : " + dto.getArticleId()));

        // 2. Récupération de la localisation
        Localisation localisation = null;
        if (dto.getLocalisationId() != null) {
            localisation = localisationRepository.findById(dto.getLocalisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Localisation introuvable"));
        }

        // 3. LOGIQUE DE GÉNÉRATION SÉQUENTIELLE (SFC-XXXXXXXX)
        long prochainId = equipementRepository.count() + 1;
        String numeroSequence = String.format("%08d", prochainId);
        String numeroSerieGenere = "SFC-" + numeroSequence;

        while (equipementRepository.findByNumeroSerie(numeroSerieGenere).isPresent()) {
            prochainId++;
            numeroSequence = String.format("%08d", prochainId);
            numeroSerieGenere = "SFC-" + numeroSequence;
        }

        // 4. Construction de l’entité
        Equipement equipement = Equipement.builder()
                .numeroSerie(numeroSerieGenere)
                .designation(dto.getDesignation())
                .article(article)
                .localisation(localisation)
                .statut(dto.getStatut() != null ? dto.getStatut() : StatutArticle.ACTIF)
                .observations(dto.getObservations())
                .dateAcquisition(dto.getDateAcquisition())
                .creePar(dto.getCreePar())
                .build();

        Equipement nouvelEquipement = equipementRepository.save(equipement);
        log.info("✅ Équipement enregistré avec le numéro de série unique : {}", nouvelEquipement.getNumeroSerie());

        // 🚨 ALERTES AUTOMATIQUES (Tâche 5) : Notification push d'intégration d'un nouveau matériel
        try {
            String messageCreation = String.format(
                    "📥 Nouvel équipement référencé : L'unité '%s' a été ajoutée à l'inventaire avec le N° de série unique %s (%s).",
                    nouvelEquipement.getDesignation(),
                    nouvelEquipement.getNumeroSerie(),
                    localisation != null ? "Localisation: " + localisation.getNom() : "Sans localisation affectée"
            );
            // Emission de l'alerte sur l'interface Angular pour la mise à jour des grilles d'inventaire
            alerteService.creerAlerte(article, messageCreation);
        } catch (Exception e) {
            log.error("⚠️ Impossible d'émettre la notification d'enregistrement d'équipement: {}", e.getMessage());
        }

        return nouvelEquipement;
    }
}