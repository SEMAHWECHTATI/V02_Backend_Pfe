package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Authentification.PreferenceNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenceNotificationRepository extends JpaRepository<PreferenceNotification, Long> {

    // 🔍 Récupérer la préférence de l'utilisateur par son ID
    Optional<PreferenceNotification> findByUtilisateurId(Long utilisateurId);
}