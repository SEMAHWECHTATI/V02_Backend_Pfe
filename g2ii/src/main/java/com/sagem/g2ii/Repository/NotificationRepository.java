package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Inventaire.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
