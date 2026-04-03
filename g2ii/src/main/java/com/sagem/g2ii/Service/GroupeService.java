package com.sagem.g2ii.Service;


import com.sagem.g2ii.Entity.Authentification.Groupe;
import com.sagem.g2ii.Repository.IntGroupe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupeService {

    @Autowired
    private IntGroupe grouperepo;

    public Groupe getGroupeById(Long id){
        return  grouperepo.findById(id).orElse(null);
    }

    public List<Groupe> getAllGroupe() {
        return grouperepo.findAll();
    }

    public Groupe  ajouterGroup(Groupe groupe){
        groupe.setDateCreation(java.time.LocalDateTime.now());
      return   grouperepo.save(groupe);
    }

    public Groupe getById(Long id) {
        return grouperepo.findById(id).orElse(null);
    }

    public void deleteGroupe(Long id) {
        grouperepo.deleteById(id);
    }

}
