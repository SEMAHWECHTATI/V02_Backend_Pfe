package com.sagem.g2ii.Repository;


import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntUtilisateur extends JpaRepository<Utilisateur,Long> {
    Optional<Utilisateur> findByEmail(String email);


}
