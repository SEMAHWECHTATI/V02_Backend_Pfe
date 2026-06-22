package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.Priorite;
import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
import com.sagem.g2ii.Entity.Intervention.SLA;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepo extends JpaRepository<Ticket, Long> {
    List<Ticket> findAll();
    List<Ticket> findByDemandeur_Id(Long idDemandeur);
    List<Ticket> findByGroupeAssigne_Id(Long idGroupe);
    List<Ticket> findByStatut(StatutTicket statut);
    List<Ticket> findByPriorite(Priorite priorite);
    List<Ticket> findBySlaRespecte(Boolean slaRespecte);
    Optional<Ticket> findTopByReferenceStartingWithOrderByReferenceDesc(String prefix);
    List<Ticket> findByStatutIn(List<StatutTicket> statuts);
    long countBySlaAssigneIsNotNullAndSlaPriseEnChargeRespecte(boolean respecte);
    long countBySlaAssigneIsNotNullAndSlaResolutionRespecte(boolean respecte);


    // Compter selon le respect global
    long countBySlaAssigneIsNotNull();
    long countBySlaAssigneIsNotNullAndSlaRespecte(Boolean slaRespecte);

    List<Ticket> findBySlaAssigne(SLA sla);

    // Compter pour la Prise en Charge
    long countBySlaPriseEnChargeRespecte(Boolean respecte);

    // Compter pour la Résolution
    long countBySlaResolutionRespecte(Boolean respecte);

    // Récupérer le taux par priorité
    @Query("SELECT t.priorite, " +
            "COUNT(CASE WHEN t.slaRespecte = true THEN 1 END) * 100.0 / COUNT(t) " +
            "FROM Ticket t WHERE t.slaAssigne IS NOT NULL GROUP BY t.priorite")
    List<Object[]> getTauxReussiteParPriorite();

    @Query("SELECT t.categorie.nomCategorie, COUNT(t) FROM Ticket t GROUP BY t.categorie.nomCategorie")
    List<Object[]> countByCategorie();

    @Query("SELECT CONCAT(t.demandeur.nom, ' ', t.demandeur.prenom), COUNT(t) FROM Ticket t GROUP BY t.demandeur.nom, t.demandeur.prenom")
    List<Object[]> countByDemandeur();

    @Query("SELECT t.technicienAssigne.email, COUNT(t) FROM Ticket t WHERE t.technicienAssigne IS NOT NULL GROUP BY t.technicienAssigne.email")
    List<Object[]> countByTechnicienAssigne();

    // Exemple pour récupérer le temps moyen de traitement (Différence entre date de clôture et création)
// Note: La fonction temporelle peut dépendre de votre dialecte SQL (Ex: EXTRACT pour PostgreSQL)
    @Query("SELECT AVG(t.delaiResolution) FROM Ticket t WHERE t.dateResolution IS NOT NULL")
    Double getAverageResolutionTimeInHours();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.statut = 'Cloture'")
    long countCloturedTickets();

    // Compte les tickets créés après une certaine date (permet de filtrer par jour/semaine/mois depuis Angular)
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.date >= :startDate")
    long countTicketsDepuis(@Param("startDate") LocalDateTime startDate);

    // ✅ Temps moyen par domaine (Catégorie) en JPQL
    @Query("SELECT t.categorie.nomCategorie, AVG(t.delaiResolution) " +
            "FROM Ticket t " +
            "WHERE t.dateResolution IS NOT NULL " +
            "GROUP BY t.categorie.nomCategorie")
    List<Object[]> getAverageResolutionTimeByDomaine();

    // ✅ Temps moyen par technicien en JPQL
    @Query("SELECT t.technicienAssigne.email, AVG(t.delaiResolution) " +
            "FROM Ticket t " +
            "WHERE t.dateResolution IS NOT NULL AND t.technicienAssigne IS NOT NULL " +
            "GROUP BY t.technicienAssigne.email")
    List<Object[]> getAverageResolutionTimeByTechnicien();

}