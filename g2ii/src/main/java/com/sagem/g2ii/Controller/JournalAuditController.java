package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Authentification.JournalAudit;
import com.sagem.g2ii.Repository.IntJournalAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin("*")
public class JournalAuditController {

    @Autowired
    private IntJournalAudit auditRepo;

    // Récupérer tous les logs pour que l'Admin puisse les voir
    @GetMapping("/all")
    public List<JournalAudit> getAllLogs() {
        return auditRepo.findAll();
    }
}
