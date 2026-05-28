package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Inventaire.Localisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalisationRepository extends JpaRepository<Localisation, Long> {
    Optional<Localisation> findByBatimentAndEtageAndBureau(String batiment, String etage, String bureau);
    List<Localisation> findByBatiment(String batiment);
}