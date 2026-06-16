package com.sagem.g2ii.Repository;


import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
import org.antlr.v4.runtime.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntUtilisateur extends JpaRepository<Utilisateur,Long> {
    Optional<Utilisateur> findByEmail(String email);


    Optional<Utilisateur> findByResetToken(String resetToken);

    // 🔍 1. Trouve tous les techniciens d'un groupe en passant par la liste des groupes de l'utilisateur
    // Spring Data comprend le mot-clé 'Containing' pour chercher dans une collection @ManyToMany
    List<Utilisateur> findByRoleAndGroupesId(roleUtilisateur role, Long groupeId);

    // 🔍 2. Trouve tous les administrateurs du système
    List<Utilisateur> findByRole(roleUtilisateur role);
}
