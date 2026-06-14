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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DetailleEquipementService {

    private final EquipementRepository equipementRepository;
    private final LocalisationRepository localisationRepository;
    private final ArticleRepository articleRepository;



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
     * Met à jour le statut et/ou la localisation physique d'un équipement
     */
    @Transactional
    public Equipement mettreAJourStatutEtLocalisation(Long equipementId, StatutArticle nouveauStatut, Long nouvelleLocalisationId) {
        Equipement equipement = obtenirDetailsEquipement(equipementId);

        // Mise à jour du statut
        if (nouveauStatut != null) {
            equipement.setStatut(nouveauStatut);

            // Logique métier : Si mis au rebut, on enregistre la date
            if (nouveauStatut == StatutArticle.A_RECYCLER) {
                equipement.setDateMiseAuRebut(LocalDateTime.now());
            }
        }

        // Mise à jour de la localisation physique si demandée
        if (nouvelleLocalisationId != null) {
            Localisation nouvelleLocalisation = localisationRepository.findById(nouvelleLocalisationId)
                    .orElseThrow(() -> new EntityNotFoundException("Localisation introuvable avec l'ID : " + nouvelleLocalisationId));
            equipement.setLocalisation(nouvelleLocalisation);
        }

        return equipementRepository.save(equipement);
    }

    @Transactional(readOnly = true)
    public List<Equipement> listerParStatut(StatutArticle statut) {
        return equipementRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<Equipement> listerParLocalisation(Long localisationId) {
        return equipementRepository.findByLocalisationId(localisationId);
    }

    // À ajouter dans DetailleEquipementService.java
    @Transactional
    public Equipement creerEquipement(EquipementRequestDto dto) {

        // 1. Récupération de l'article (Obligatoire)
        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable avec l'ID : " + dto.getArticleId()));

        // 2. Récupération de la localisation (Optionnelle ou Obligatoire selon vos règles)
        Localisation localisation = null;
        if (dto.getLocalisationId() != null) {
            localisation = localisationRepository.findById(dto.getLocalisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Localisation introuvable"));
        }

        // 3. 💡 LOGIQUE DE GÉNÉRATION SÉQUENTIELLE (SFC-XXXXXXXX)
        // On compte combien d'équipements existent en base pour déterminer le numéro suivant
        long prochainId = equipementRepository.count() + 1;

        // On formate le nombre sur 8 chiffres remplis de zéros à gauche (ex: 00000001, 00000042)
        String numeroSequence = String.format("%08d", prochainId);
        String numeroSerieGenere = "SFC-" + numeroSequence;

        // Double sécurité au cas où (optionnel mais recommandé dans un environnement multi-utilisateurs)
        while (equipementRepository.findByNumeroSerie(numeroSerieGenere).isPresent()) {
            prochainId++;
            numeroSequence = String.format("%08d", prochainId);
            numeroSerieGenere = "SFC-" + numeroSequence;
        }

        // 4. Construction de l’entité
        Equipement equipement = Equipement.builder()
                .numeroSerie(numeroSerieGenere) // 👈 On injecte le numéro de série généré ici automatiquement
                .designation(dto.getDesignation())
                .article(article)
                .localisation(localisation)
                .statut(dto.getStatut() != null ? dto.getStatut() : StatutArticle.ACTIF)
                .observations(dto.getObservations())
                .dateAcquisition(dto.getDateAcquisition())
                .creePar(dto.getCreePar())
                .build();

        // Note : Le codeBarres et la dateCreation seront générés automatiquement via le @PrePersist de l'entité.
        return equipementRepository.save(equipement);
    }
}