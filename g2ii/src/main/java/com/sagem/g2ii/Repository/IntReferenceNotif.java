package com.sagem.g2ii.Repository;


import com.sagem.g2ii.Entity.Authentification.PreferenceNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntReferenceNotif extends JpaRepository<PreferenceNotification, Long> {
    Optional<PreferenceNotification> findByUtilisateurId(Long userId);}
