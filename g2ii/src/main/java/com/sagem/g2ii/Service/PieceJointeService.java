package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Intervention.PieceJointe;
import com.sagem.g2ii.Repository.PieceJointeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class PieceJointeService {

    @Autowired
    private PieceJointeRepo pieceJointeRepo;

    private final String CHEMIN_DOSSIER = "C:/g2ii_uploads/"; // A adapter selon l'environnement

    public PieceJointe uploaderFichier(Long idTicket, MultipartFile fichier) throws IOException {
        // Vérification de la taille (Ex: max 5MB défini dans application.properties, ou via code)
        if (fichier.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Le fichier dépasse la taille maximale autorisée (5MB).");
        }

        // Créer le dossier s'il n'existe pas
        File dossier = new File(CHEMIN_DOSSIER);
        if (!dossier.exists()) dossier.mkdirs();

        String nomFichier = System.currentTimeMillis() + "_" + fichier.getOriginalFilename();
        String cheminComplet = CHEMIN_DOSSIER + nomFichier;

        // Sauvegarde physique
        fichier.transferTo(new File(cheminComplet));

        // Enregistrement en base de données
        PieceJointe pieceJointe = new PieceJointe();
        pieceJointe.setNomJointe(fichier.getOriginalFilename());
        pieceJointe.setCheminStockage(cheminComplet);
        pieceJointe.setTaille(fichier.getSize());
        pieceJointe.setDate(LocalDate.now());
        // pieceJointe.setTicket(...); // Lier au ticket

        return pieceJointeRepo.save(pieceJointe);
    }
}
