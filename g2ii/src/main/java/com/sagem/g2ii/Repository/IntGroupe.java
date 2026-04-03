package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntGroupe extends JpaRepository<Groupe, Long> {

    Optional<Groupe> findByNomGroupes(GroupeTechnicien nomGroupes);}
