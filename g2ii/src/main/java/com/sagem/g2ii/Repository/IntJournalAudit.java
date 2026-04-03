package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Authentification.JournalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntJournalAudit extends JpaRepository<JournalAudit, Long> {

}
