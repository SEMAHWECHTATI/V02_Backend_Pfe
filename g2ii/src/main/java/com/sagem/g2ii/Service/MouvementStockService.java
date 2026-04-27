package com.sagem.g2ii.Service;

import com.sagem.g2ii.DTOs.MouvementStockDTO;
import com.sagem.g2ii.Entity.Enumeration.TypeMouvement;
import com.sagem.g2ii.Entity.Inventaire.MouvementStock;
import com.sagem.g2ii.Entity.Inventaire.Stock;
import com.sagem.g2ii.Repository.ArticleRepository;
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

    /**
     * ✅ Enregistrer une entrée
     */
    public MouvementStockDTO enregistrerEntree(Long stockId, Integer quantite, String justification) {
        log.info("📥 Enregistrement entrée: {} articles", quantite);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        MouvementStock mouvement = MouvementStock.builder()
                .type(TypeMouvement.ENTREE)
                .quantite(quantite)
                .justification(justification)
                .stock(stock)
                .build();

        MouvementStock saved = mouvementRepository.save(mouvement);

        // Mettre à jour la quantité
        Integer nouvelleQuantite = stock.getQuantiteEnStock() + quantite;
        stockService.mettreAJourQuantite(stockId, nouvelleQuantite);

        log.info("✅ Entrée enregistrée: {}", saved.getId());
        return convertToDTO(saved);
    }

    /**
     * ✅ Enregistrer une sortie
     */
    public MouvementStockDTO enregistrerSortie(Long stockId, Integer quantite, String justification) {
        log.info("📤 Enregistrement sortie: {} articles", quantite);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        if (stock.getQuantiteEnStock() < quantite) {
            throw new RuntimeException("Quantité insuffisante en stock");
        }

        MouvementStock mouvement = MouvementStock.builder()
                .type(TypeMouvement.SORTIE)
                .quantite(quantite)
                .justification(justification)
                .stock(stock)
                .build();

        MouvementStock saved = mouvementRepository.save(mouvement);

        // Mettre à jour la quantité
        Integer nouvelleQuantite = stock.getQuantiteEnStock() - quantite;
        stockService.mettreAJourQuantite(stockId, nouvelleQuantite);

        log.info("✅ Sortie enregistrée: {}", saved.getId());
        return convertToDTO(saved);
    }

    /**
     * ✅ Enregistrer un transfert
     */
    public MouvementStockDTO enregistrerTransfert(Long stockId, Integer quantite,
                                                  String locSource, String locDest,
                                                  String justification) {
        log.info("🔄 Enregistrement transfert: {} -> {}", locSource, locDest);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        MouvementStock mouvement = MouvementStock.builder()
                .type(TypeMouvement.TRANSFERT)
                .quantite(quantite)
                .justification(justification)
                .localisationSource(locSource)
                .localisationDestination(locDest)
                .stock(stock)
                .build();

        MouvementStock saved = mouvementRepository.save(mouvement);
        log.info("✅ Transfert enregistré: {}", saved.getId());

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
