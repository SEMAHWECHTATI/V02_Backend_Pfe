package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Authentification.Utilisateur;
import com.sagem.g2ii.Entity.Intervention.PieceJointe;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import com.sagem.g2ii.Repository.IntUtilisateur;
import com.sagem.g2ii.Repository.PieceJointeRepo;
import com.sagem.g2ii.Repository.TicketRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PieceJointeService {

    // ✅ DÉCLARER LE LOGGER
    private static final Logger logger = LoggerFactory.getLogger(PieceJointeService.class);

    @Autowired
    private PieceJointeRepo pieceJointeRepo;

    @Autowired
    private TicketRepo ticketRepository;

    @Autowired
    private IntUtilisateur utilisateurRepository;

    private final String CHEMIN_DOSSIER = "D:/Test pfe 2026/g2ii/g2ii_uploads/";

    /**
     * 📎 UPLOADER UN FICHIER AVEC ID UTILISATEUR
     */
    public PieceJointe uploaderFichier(Long idTicket, Long idUtilisateur, MultipartFile fichier) throws IOException {
        logger.info("📎 Upload fichier: " + fichier.getOriginalFilename() + " par utilisateur: " + idUtilisateur);

        // ✅ Vérifier la taille
        if (fichier.getSize() > 5 * 1024 * 1024) {
            logger.error("❌ Fichier dépasse 5MB: " + fichier.getOriginalFilename());
            throw new RuntimeException("❌ Le fichier dépasse 5MB");
        }

        // ✅ Récupérer le ticket
        Optional<Ticket> ticketOpt = ticketRepository.findById(idTicket);
        if (ticketOpt.isEmpty()) {
            logger.error("❌ Ticket non trouvé: " + idTicket);
            throw new RuntimeException("❌ Ticket non trouvé: " + idTicket);
        }
        Ticket ticket = ticketOpt.get();

        // ✅ RÉCUPÉRER L'UTILISATEUR
        Utilisateur utilisateur = null;
        if (idUtilisateur != null && idUtilisateur > 0) {
            Optional<Utilisateur> userOpt = utilisateurRepository.findById(idUtilisateur);
            if (userOpt.isPresent()) {
                utilisateur = userOpt.get();
                logger.info("✅ Utilisateur trouvé: " + utilisateur.getPrenom() + " " + utilisateur.getNom());
            } else {
                logger.warn("⚠️ Utilisateur non trouvé: " + idUtilisateur);
            }
        }

        // ✅ Créer le dossier s'il n'existe pas
        File dossier = new File(CHEMIN_DOSSIER);
        if (!dossier.exists()) {
            dossier.mkdirs();
            logger.info("📁 Dossier créé: " + CHEMIN_DOSSIER);
        }

        // ✅ Sauvegarder le fichier physiquement
        String nomFichier = System.currentTimeMillis() + "_" + fichier.getOriginalFilename();
        String cheminComplet = CHEMIN_DOSSIER + nomFichier;
        fichier.transferTo(new File(cheminComplet));
        logger.info("💾 Fichier sauvegardé: " + cheminComplet);

        // ✅ Extraire l'extension
        String extension = fichier.getOriginalFilename()
                .substring(fichier.getOriginalFilename().lastIndexOf(".") + 1)
                .toLowerCase();

        // ✅ Créer l'entité PieceJointe
        PieceJointe pieceJointe = new PieceJointe();
        pieceJointe.setNomJointe(fichier.getOriginalFilename());
        pieceJointe.setCheminStockage(cheminComplet);
        pieceJointe.setTaille(fichier.getSize());
        pieceJointe.setDate(LocalDateTime.now());  // ✅ Utiliser setDate() pour LocalDateTime
        pieceJointe.setTypefichier(extension);
        pieceJointe.setTicket(ticket);
        pieceJointe.setUtilisateur(utilisateur);

        // ✅ Sauvegarder en BD
        PieceJointe saved = pieceJointeRepo.save(pieceJointe);
        logger.info("✅ PieceJointe enregistrée avec ID: " + saved.getIdJointe());

        return saved;
    }

    /**
     * 📎 UPLOADER SANS ID UTILISATEUR (Compatibilité)
     */
    public PieceJointe uploaderFichier(Long idTicket, MultipartFile fichier) throws IOException {
        return uploaderFichier(idTicket, null, fichier);
    }

    /**
     * 📋 RÉCUPÉRER LES PIÈCES D'UN TICKET
     */
    public List<PieceJointe> getPiecesByTicket(Long idTicket) {
        logger.info("📋 Récupération pièces jointes du ticket: " + idTicket);
        return pieceJointeRepo.findByTicketIdTicket(idTicket);
    }

    /**
     * 👤 RÉCUPÉRER LES PIÈCES D'UN UTILISATEUR
     */
    public List<PieceJointe> getPiecesByUtilisateur(Long idUtilisateur) {
        logger.info("📋 Récupération pièces jointes de l'utilisateur: " + idUtilisateur);
        return pieceJointeRepo.findByUtilisateurId(idUtilisateur);
    }

    /**
     * 📎 RÉCUPÉRER LES PIÈCES D'UN TICKET PAR UN UTILISATEUR SPÉCIFIQUE
     */
    public List<PieceJointe> getPiecesByTicketAndUtilisateur(Long idTicket, Long idUtilisateur) {
        logger.info("📋 Récupération pièces du ticket " + idTicket + " par utilisateur " + idUtilisateur);
        return pieceJointeRepo.findByTicketAndUtilisateur(idTicket, idUtilisateur);
    }

    /**
     * 📊 COMPTER LES PIÈCES D'UN TICKET
     */
    public Long countPiecesByTicket(Long idTicket) {
        return pieceJointeRepo.countByTicketIdTicket(idTicket);
    }

    /**
     * 🔍 RÉCUPÉRER UNE PIÈCE JOINTE PAR ID
     */
    public Optional<PieceJointe> getPieceJointeById(Long idPieceJointe) {
        logger.info("🔍 Récupération pièce jointe ID: " + idPieceJointe);
        return pieceJointeRepo.findById(idPieceJointe);
    }

    /**
     * 🗑️ SUPPRIMER UNE PIÈCE JOINTE
     */
    public void supprimerPieceJointe(Long idPieceJointe) {
        logger.info("🗑️ Suppression pièce jointe: " + idPieceJointe);

        Optional<PieceJointe> pieceOpt = pieceJointeRepo.findById(idPieceJointe);
        if (pieceOpt.isPresent()) {
            PieceJointe piece = pieceOpt.get();

            // ✅ Supprimer le fichier physique
            File file = new File(piece.getCheminStockage());
            if (file.exists()) {
                if (file.delete()) {
                    logger.info("✅ Fichier supprimé: " + piece.getCheminStockage());
                } else {
                    logger.error("❌ Erreur suppression fichier: " + piece.getCheminStockage());
                }
            }

            // ✅ Supprimer de la BD
            pieceJointeRepo.deleteById(idPieceJointe);
            logger.info("✅ PieceJointe supprimée de la BD");
        }
    }

    /**
     * 🗑️ SUPPRIMER TOUTES LES PIÈCES D'UN TICKET
     */
    public void supprimerPiecesByTicket(Long idTicket) {
        logger.info("🗑️ Suppression toutes pièces du ticket: " + idTicket);

        List<PieceJointe> pieces = getPiecesByTicket(idTicket);
        for (PieceJointe piece : pieces) {
            supprimerPieceJointe(piece.getIdJointe());
        }
    }
}