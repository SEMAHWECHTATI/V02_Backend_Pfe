package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepo extends JpaRepository<Ticket, Long> {

    List<Ticket> findByDemandeur_Id(Long idDemandeur);
    List<Ticket> findByGroupeAssigne_Id(Long idGroupe);
    List<Ticket> findByStatut(StatutTicket statut);
    List<Ticket> findByPriorite(Priorite priorite);
    List<Ticket> findBySlaRespecte(Boolean slaRespecte);
    Optional<Ticket> findTopByReferenceStartingWithOrderByReferenceDesc(String prefix);
    List<Ticket> findByStatutIn(List<StatutTicket> statuts);


    // Compter selon le respect global
    long countBySlaAssigneIsNotNull();
    long countBySlaAssigneIsNotNullAndSlaRespecte(Boolean slaRespecte);

    // Compter pour la Prise en Charge
    long countBySlaPriseEnChargeRespecte(Boolean respecte);

    // Compter pour la Résolution
    long countBySlaResolutionRespecte(Boolean respecte);

    // Récupérer le taux par priorité
    @Query("SELECT t.priorite, " +
            "COUNT(CASE WHEN t.slaRespecte = true THEN 1 END) * 100.0 / COUNT(t) " +
            "FROM Ticket t WHERE t.slaAssigne IS NOT NULL GROUP BY t.priorite")
    List<Object[]> getTauxReussiteParPriorite();

}