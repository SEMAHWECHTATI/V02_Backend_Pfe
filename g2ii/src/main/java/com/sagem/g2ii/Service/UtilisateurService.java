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

    public Utilisateur updateUser(Long id, Utilisateur user) {
        user.setId(id);
        return utilisateurRepo.save(user);
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