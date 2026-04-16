package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Email.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {
    /**
     * Cette méthode est CRUCIALE :
     * Elle permet de récupérer uniquement les e-mails qui :
     * 1. N'ont pas encore été envoyés (envoye = false)
     * 2. N'ont pas dépassé le nombre maximum de tentatives (ex: moins de 5 essais)
     */
    List<EmailQueue> findByEnvoyeFalseAndTentativesLessThan(int maxTentatives);
}
