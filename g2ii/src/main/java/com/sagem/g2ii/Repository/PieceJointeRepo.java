package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Intervention.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceJointeRepo extends JpaRepository<PieceJointe, Long> {

    // ✅ RÉCUPÉRER LES PIÈCES D'UN TICKET
    List<PieceJointe> findByTicketIdTicket(Long idTicket);

    // ✅ RÉCUPÉRER LES PIÈCES AJOUTÉES PAR UN UTILISATEUR
    List<PieceJointe> findByUtilisateurId(Long idUtilisateur);

    // ✅ RÉCUPÉRER LES PIÈCES D'UN TICKET PAR UN UTILISATEUR
    @Query("SELECT pj FROM PieceJointe pj WHERE pj.ticket.idTicket = :idTicket AND pj.utilisateur.id = :idUtilisateur")
    List<PieceJointe> findByTicketAndUtilisateur(@Param("idTicket") Long idTicket, @Param("idUtilisateur") Long idUtilisateur);

    // ✅ COMPTER LES PIÈCES D'UN TICKET
    Long countByTicketIdTicket(Long idTicket);



}


