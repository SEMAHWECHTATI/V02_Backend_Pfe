package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface IntGroupe extends JpaRepository<Groupe, Long> {

    Optional<Groupe> findByNomGroupes(GroupeTechnicien nomGroupes);

    List<Groupe> findByActifTrue();


}