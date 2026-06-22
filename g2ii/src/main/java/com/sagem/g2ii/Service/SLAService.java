package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Intervention.Categorie;
import com.sagem.g2ii.Entity.Intervention.SLA;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.SLARepository;
import com.sagem.g2ii.Repository.TicketRepo; // 👈 Utilise votre vrai nom de repository
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SLAService {

    private final SLARepository slaRepository;
    private final TicketRepo ticketRepository; // 👈 Injecté proprement via @RequiredArgsConstructor

    @Transactional(readOnly = true)
    public List<SLA> obtenirTousLesSLA() {
        return slaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SLA obtenirSlaParId(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Règle SLA introuvable avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public List<SLA> obtenirSlasParCategorie(Long idCategorie) {
        return slaRepository.findByCategorieIdCategorie(idCategorie);
    }

    /**
     * Moteur de recherche SLA utilisé par votre application pour lier un Ticket à ses délais
     */
    @Transactional(readOnly = true)
    public SLA obtenirSLA(Categorie categorie, Priorite priorite) {
        if (categorie == null || priorite == null) {
            System.out.println("   ❌ Recherche SLA avortée : Catégorie ou Priorité nulle.");
            return null;
        }

        System.out.println("🔍 [OBTENIR SLA] Recherche pour: " + categorie.getNomCategorie() + " | Priorité: " + priorite);

        // ✅ Utilisation de l'Optional pour éviter les NullPointerException de compilation
        Optional<SLA> slaApplicableOpt = slaRepository.findByCategorieAndPriorite(categorie, priorite);

        if (slaApplicableOpt.isPresent()) {
            SLA slaApplicable = slaApplicableOpt.get();
            System.out.println("   ✅ SLA trouvé: " + slaApplicable.getNomSLA() + " (ID: " + slaApplicable.getIdSLA() + ")");
            return slaApplicable;
        } else {
            System.out.println("   ❌ Aucun SLA trouvé!");
            System.out.println("      Catégorie: " + categorie.getNomCategorie() + " (ID: " + categorie.getIdCategorie() + ")"); // 💡 Retrait du shortValue()
            System.out.println("      Priorité: " + priorite);
            System.out.println("      → Vérifiez que la table SLA contient une ligne correspondant à cette combinaison");
            return null;
        }
    }
    @Transactional
    public SLA enregistrerSLA(SLA sla) {
        log.info("💾 Enregistrement d'une règle SLA : {}", sla.getNomSLA());
        return slaRepository.save(sla);
    }

    @Transactional
    public SLA modifierSLA(Long id, SLA slaDetails) {
        SLA existingSla = obtenirSlaParId(id);
        existingSla.setNomSLA(slaDetails.getNomSLA());
        existingSla.setDelaiPriseEnChargeHeure(slaDetails.getDelaiPriseEnChargeHeure());
        existingSla.setDelaiResolutionHeure(slaDetails.getDelaiResolutionHeure());
        existingSla.setPriorite(slaDetails.getPriorite());
        return slaRepository.save(existingSla);
    }

    /**
     * 🛡️ Supprime un SLA en toute sécurité sans rompre l'intégrité de la BDD (Contrainte 23503 évitée)
     */
    @Transactional
    public void supprimerSLA(Long idSLA) {
        log.info("🗑️ Tentative de suppression sécurisée du SLA ID : {}", idSLA);

        // 1. Vérifier si le SLA existe
        SLA sla = slaRepository.findById(idSLA)
                .orElseThrow(() -> new EntityNotFoundException("SLA introuvable avec l'id : " + idSLA));

        // 2. Récupérer tous les tickets liés à ce SLA
        List<Ticket> ticketsLies = ticketRepository.findBySlaAssigne(sla);

        // 3. Rompre le lien en mettant le champ slaAssigne à null pour chaque ticket concerné
        if (ticketsLies != null && !ticketsLies.isEmpty()) {
            log.info("   🔗 {} ticket(s) lié(s) détecté(s). Désindexation de la clé étrangère en cours...", ticketsLies.size());
            for (Ticket ticket : ticketsLies) {
                ticket.setSlaAssigne(null); // On détache le SLA sans supprimer le ticket historique !
            }
            ticketRepository.saveAll(ticketsLies); // Sauvegarde groupée des modifications
        }

        // 4. Maintenant que plus aucune ligne de la table "ticket" ne pointe dessus, la suppression réussit !
        slaRepository.delete(sla);
        log.info("   ✅ SLA supprimé définitivement de la base de données.");
    }
}