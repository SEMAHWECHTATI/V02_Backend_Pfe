package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.MouvementStockDTO;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.TypeMouvement;
import com.sagem.g2ii.Entity.Inventaire.MouvementStock;
import com.sagem.g2ii.Entity.Inventaire.Stock;
import com.sagem.g2ii.Repository.ArticleRepository;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Repository.MouvementStockRepository;
import com.sagem.g2ii.Repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MouvementStockService {

    private final MouvementStockRepository mouvementRepository;
    private final StockRepository stockRepository;
    private final ArticleRepository articleRepository;
    private final StockService stockService;
    private final IntUtilisateur utilisateurrepo;

    /**
     * ✅ Enregistrer une entrée
     */
    @Transactional
    public MouvementStockDTO enregistrerEntree(Long stockId, Integer quantite, String justification, Long responsableId, String referenceTicket) {
        log.info("📥 Enregistrement d'une entrée de {} articles pour le stock ID: {}", quantite, stockId);

        // 1. Récupérer le compteur de stock existant
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec l'ID : " + stockId));

        // 2. Récupérer le responsable de l'entrée (Exigence réglementaire / Audit)
        Utilisateur responsable = null;
        if (responsableId != null) {
            responsable = utilisateurrepo.findById(responsableId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur responsable non trouvé avec l'ID : " + responsableId));
        }

        // 3. Construire le mouvement d'entrée avec ses liaisons
        MouvementStock mouvement = MouvementStock.builder()
                .type(TypeMouvement.ENTREE)
                .quantite(quantite)
                .justification(justification)
                .stock(stock)
                .responsable(responsable)       // 👈 Ajouté
                .referenceTicket(referenceTicket) // 👈 Ajouté
                .build();

        MouvementStock saved = mouvementRepository.save(mouvement);

        // 4. Mettre à jour (incrémenter) la quantité globale en stock
        Integer nouvelleQuantite = stock.getQuantiteEnStock() + quantite;
        stockService.mettreAJourQuantite(stockId, nouvelleQuantite);

        log.info("✅ Entrée enregistrée avec succès. Mouvement ID: {}. Nouveau stock total: {}", saved.getId(), nouvelleQuantite);
        return convertToDTO(saved);
    }

    /**
     * ✅ Enregistrer une sortie
     */
    @Transactional
    public MouvementStockDTO enregistrerSortie(Long stockId, Integer quantite, String justification, Long responsableId, String referenceTicket) {
        log.info("📤 Enregistrement d'une sortie de {} articles pour le stock ID: {}", quantite, stockId);

        // 1. Récupérer le stock actuel
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec l'ID : " + stockId));

        // 2. Vérifier le solde disponible
        if (stock.getQuantiteEnStock() < quantite) {
            throw new RuntimeException("Action impossible : Quantité insuffisante en stock (" + stock.getQuantiteEnStock() + " disponibles).");
        }

        // 3. Récupérer le responsable de l'action (Exigence de traçabilité)
        Utilisateur responsable = null;
        if (responsableId != null) {
            responsable = utilisateurrepo.findById(responsableId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur responsable non trouvé avec l'ID : " + responsableId));
        }

        // 4. Construire le mouvement avec toutes ses métadonnées
        MouvementStock mouvement = MouvementStock.builder()
                .type(TypeMouvement.SORTIE)
                .quantite(quantite)
                .justification(justification)
                .stock(stock)
                .responsable(responsable)       // 👈 Ajouté
                .referenceTicket(referenceTicket) // 👈 Ajouté
                .build();

        MouvementStock saved = mouvementRepository.save(mouvement);

        // 5. Mettre à jour la quantité globale du stock associé
        Integer nouvelleQuantite = stock.getQuantiteEnStock() - quantite;
        stockService.mettreAJourQuantite(stockId, nouvelleQuantite);

        log.info("✅ Sortie enregistrée avec succès. Mouvement ID: {}. Nouveau stock total: {}", saved.getId(), nouvelleQuantite);
        return convertToDTO(saved);
    }

    /**
     * ✅ Enregistrer un transfert
     */
    @Transactional
    public MouvementStockDTO enregistrerTransfert(Long stockId, Integer quantite,
                                                  String locSource, String locDest,
                                                  String justification, Long responsableId,
                                                  String referenceTicket) {
        log.info("🔄 Enregistrement transfert de {} articles: {} -> {}", quantite, locSource, locDest);

        // 1. Récupérer le stock concerné
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec l'ID : " + stockId));

        // 2. Validation de sécurité : Vérifier le solde
        if (stock.getQuantiteEnStock() < quantite) {
            throw new RuntimeException("Transfert impossible : Quantité insuffisante en stock ("
                    + stock.getQuantiteEnStock() + " disponibles pour " + quantite + " demandés).");
        }

        // 3. Récupérer le responsable
        Utilisateur responsable = null;
        if (responsableId != null) {
            responsable = utilisateurrepo.findById(responsableId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur responsable non trouvé avec l'ID : " + responsableId));
        }

        // 4. Construire et sauvegarder le mouvement (La traçabilité s'écrit ici !)
        MouvementStock mouvement = MouvementStock.builder()
                .type(TypeMouvement.TRANSFERT)
                .quantite(quantite)
                .justification(justification)
                .localisationSource(locSource)
                .localisationDestination(locDest)
                .stock(stock)
                .responsable(responsable)
                .referenceTicket(referenceTicket)
                .build();

        MouvementStock saved = mouvementRepository.save(mouvement);

        // 💡 REMARQUE : On ne touche pas à stock.setLocalisation() car le compteur global
        // du stock reste inchangé lors d'un transfert interne. La quantité totale ne bouge pas.

        log.info("✅ Transfert enregistré avec succès en historique. Mouvement ID: {}", saved.getId());
        return convertToDTO(saved);
    }

    /**
     * ✅ Lier mouvement à ticket
     */
    public void lierMouvementATicket(Long mouvementId, String referenceTicket) {
        MouvementStock mouvement = mouvementRepository.findById(mouvementId)
                .orElseThrow(() -> new RuntimeException("Mouvement non trouvé"));

        mouvement.setReferenceTicket(referenceTicket);
        mouvementRepository.save(mouvement);

        log.info("🔗 Mouvement lié au ticket: {}", referenceTicket);
    }

    /**
     * ✅ Historique mouvements par stock
     */
    public List<MouvementStockDTO> getHistoriqueMouvements(Long stockId) {
        return mouvementRepository.findByStockId(stockId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Mouvements entre deux dates
     */
    public List<MouvementStockDTO> getMouvementsBetweenDates(LocalDateTime debut, LocalDateTime fin) {
        return mouvementRepository.findMouvementsBetweenDates(debut, fin).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔄 Convertir Entity en DTO
     */
    private MouvementStockDTO convertToDTO(MouvementStock mouvement) {
        return MouvementStockDTO.builder()
                .id(mouvement.getId())
                .type(mouvement.getType())
                .quantite(mouvement.getQuantite())
                .justification(mouvement.getJustification())
                .localisationSource(mouvement.getLocalisationSource())
                .localisationDestination(mouvement.getLocalisationDestination())
                .dateMouvement(mouvement.getDateMouvement())
                .stockId(mouvement.getStock().getId())
                .articleId(mouvement.getStock().getArticle().getId())
                .articleDesignation(mouvement.getStock().getArticle().getDesignation())
                .responsableId(mouvement.getResponsable() != null ? mouvement.getResponsable().getId() : null)
                .responsableName(mouvement.getResponsable() != null ?
                        mouvement.getResponsable().getPrenom() + " " + mouvement.getResponsable().getNom() : null)
                .referenceTicket(mouvement.getReferenceTicket())
                .build();
    }
}
