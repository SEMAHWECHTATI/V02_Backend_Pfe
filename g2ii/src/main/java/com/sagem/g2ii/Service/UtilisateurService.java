package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Repository.IntGroupe;
import com.sagem.g2ii.Repository.IntUtilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    @Autowired
    private IntUtilisateur utilisateurRepo;

    @Autowired
    private IntGroupe groupeRepo;

    public Utilisateur addUser(Utilisateur user) {
        return utilisateurRepo.save(user);
    }

    public List<Utilisateur> getAllUsers() {
        return utilisateurRepo.findAll();
    }

    public Utilisateur getUserById(Long id) {
        return utilisateurRepo.findById(id).orElse(null);
    }

    public void deleteUserById(Long id) {
        utilisateurRepo.deleteById(id);
    }

    public Utilisateur updateUser(Long id, Utilisateur userDetails) {
        // 1. On récupère l'utilisateur actuel en base de données
        return utilisateurRepo.findById(id).map(existingUser -> {

            // 2. On met à jour uniquement les champs autorisés
            existingUser.setNom(userDetails.getNom());
            existingUser.setPrenom(userDetails.getPrenom());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setTelephone(userDetails.getTelephone());
            existingUser.setMatricule(userDetails.getMatricule());
            existingUser.setRole(userDetails.getRole());
            existingUser.setStatut(userDetails.getStatut());
            existingUser.setDepartement(userDetails.getDepartement());

            // On NE touche PAS à existingUser.setMotDePasse(...)
            // ainsi le mot de passe actuel est conservé en base.

            // 3. On sauvegarde l'objet existant qui est toujours "complet"
            return utilisateurRepo.save(existingUser);

        }).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));
    }

    public void ajouterUtilisateurAuGroupe(Long userId, Long groupeId) {
        Utilisateur utilisateur = utilisateurRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID : " + userId));

        Groupe groupe = groupeRepo.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable avec l'ID : " + groupeId));

        utilisateur.getGroupes().add(groupe);
        utilisateurRepo.save(utilisateur);
    }
}