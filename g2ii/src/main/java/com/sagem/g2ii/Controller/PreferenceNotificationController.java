package com.sagem.g2ii.Controller;


import com.sagem.g2ii.Entity.Authentification.PreferenceNotification;
import com.sagem.g2ii.Repository.IntReferenceNotif;
import com.sagem.g2ii.Repository.IntUtilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@CrossOrigin("*")
public class PreferenceNotificationController {

    @Autowired
    private IntReferenceNotif preferenceRepo;

    @Autowired
    private IntUtilisateur utilisateurRepo;

    // 1. Récupérer les préférences d'un utilisateur spécifique
    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<PreferenceNotification> getByUtilisateur(@PathVariable Long userId) {
        return preferenceRepo.findByUtilisateurId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. Créer les préférences pour un utilisateur
    @PostMapping("/creer/{userId}")
    public ResponseEntity<?> createPreferences(
            @PathVariable Long userId,
            @RequestBody PreferenceNotification newPrefs) {

        if (preferenceRepo.findByUtilisateurId(userId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Des preferences existent deja pour cet utilisateur.");
        }

        var userOpt = utilisateurRepo.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Utilisateur introuvable.");
        }

        PreferenceNotification prefs = PreferenceNotification.builder()
            .utilisateur(userOpt.get())
            .typeAlerte(newPrefs.getTypeAlerte())
            .canalEmail(newPrefs.isCanalEmail())
            .canalInApp(newPrefs.isCanalInApp())
            .actif(newPrefs.isActif())
            .build();

        return new ResponseEntity<>(preferenceRepo.save(prefs), HttpStatus.CREATED);
    }

    // 3. Mettre à jour les préférences (ex: désactiver les emails)
    @PutMapping("/update/{id}")
    public ResponseEntity<PreferenceNotification> updatePreferences(
            @PathVariable Long id,
            @RequestBody PreferenceNotification newPrefs) {

        return preferenceRepo.findById(id)
                .map(prefs -> {
                    prefs.setCanalEmail(newPrefs.isCanalEmail());
                    prefs.setCanalInApp(newPrefs.isCanalInApp());
                    prefs.setActif(newPrefs.isActif());
                    prefs.setTypeAlerte(newPrefs.getTypeAlerte());
                    return ResponseEntity.ok(preferenceRepo.save(prefs));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
