package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Enumeration.Severite;
import com.sagem.g2ii.Entity.Enumeration.StatutAlerte;
import com.sagem.g2ii.Entity.Enumeration.TypeAlerte;
import com.sagem.g2ii.Entity.Inventaire.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    List<Alerte> findByType(TypeAlerte type);
    List<Alerte> findByStatut(StatutAlerte statut);
    List<Alerte> findBySeverite(Severite severite);
    List<Alerte> findByStatutAndSeveriteOrderByDateCreationDesc(StatutAlerte statut, Severite severite);
}