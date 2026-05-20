package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.StatutDemande;
import com.sagem.g2ii.Entity.Inventaire.DemandeMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DemandeMaterielRepository extends JpaRepository<DemandeMateriel,Long> {

    // ✅ Récupérer par référence
    Optional<DemandeMateriel> findByReference(String reference);

    // ✅ Récupérer demandes par statut
    List<DemandeMateriel> findByStatut(StatutDemande statut);

    // ✅ Récupérer demandes en attente de validation
    @Query("SELECT d FROM DemandeMateriel d WHERE d.statut = 'EN_ATTENTE' ORDER BY d.dateCreation DESC")
    List<DemandeMateriel> findDemandesEnAttente();

    // ✅ Récupérer demandes validées gestionnaire en attente admin
    @Query("SELECT d FROM DemandeMateriel d WHERE d.statut = 'VALIDE_GESTIONNAIRE' ORDER BY d.dateCreation DESC")
    List<DemandeMateriel> findDemandesValideeGestionnaire();

    // ✅ Récupérer demandes par utilisateur demandeur
    List<DemandeMateriel> findByUtilisateurDemandeurId(Long utilisateurId);

    // ✅ Récupérer demandes non consommées
    @Query("SELECT d FROM DemandeMateriel d WHERE d.statut != 'CONSOMME' AND d.statut != 'REJETE' AND d.statut != 'ANNULE'")
    List<DemandeMateriel> findDemandesNonConsommees();

    // ✅ Compter demandes en attente
    @Query("SELECT COUNT(d) FROM DemandeMateriel d WHERE d.statut = 'EN_ATTENTE'")
    Long countDemandesEnAttente();

    // ✅ Compter demandes à valider par admin
    @Query("SELECT COUNT(d) FROM DemandeMateriel d WHERE d.statut = 'VALIDE_GESTIONNAIRE'")
    Long countDemandesValideeGestionnaire();


}
