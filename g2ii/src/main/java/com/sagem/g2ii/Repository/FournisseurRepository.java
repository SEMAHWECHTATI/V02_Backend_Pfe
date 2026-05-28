package com.sagem.g2ii.Repository;
import com.sagem.g2ii.Entity.Inventaire.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    Optional<Fournisseur> findByNom(String nom);
}
