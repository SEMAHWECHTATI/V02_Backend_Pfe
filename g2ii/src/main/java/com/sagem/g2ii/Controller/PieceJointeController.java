package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Intervention.PieceJointe;
import com.sagem.g2ii.Service.PieceJointeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;  // ✅ AJOUTER
import org.springframework.core.io.Resource;  // ✅ AJOUTER
import org.springframework.http.HttpHeaders;  // ✅ AJOUTER
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;  // ✅ AJOUTER
import org.slf4j.LoggerFactory;  // ✅ AJOUTER

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fichiers")
@CrossOrigin("*")
public class PieceJointeController {

    // ✅ AJOUTER LE LOGGER
    private static final Logger logger = LoggerFactory.getLogger(PieceJointeController.class);

    @Autowired
    private PieceJointeService pieceJointeService;

    /**
     * 📎 UPLOADER UN FICHIER AVEC ID UTILISATEUR
     */
    @PostMapping("/upload/ticket/{idTicket}")
    public ResponseEntity<?> uploaderFichier(
            @PathVariable Long idTicket,
            @RequestParam("file") MultipartFile fichier,
            @RequestParam(value = "idUtilisateur", required = false) Long idUtilisateur) {
        try {
            logger.info("📎 POST /upload/ticket/" + idTicket + " - User: " + idUtilisateur);

            PieceJointe pj = pieceJointeService.uploaderFichier(idTicket, idUtilisateur, fichier);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ Fichier uploadé avec succès");
            response.put("data", pj);
            response.put("idJointe", pj.getIdJointe());
            response.put("nomFichier", pj.getNomJointe());
            response.put("taille", pj.getTaille());
            response.put("utilisateur", pj.getNomUtilisateur());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur upload: " + e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur d'upload: " + e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 📋 RÉCUPÉRER LES PIÈCES D'UN TICKET
     */
    @GetMapping("/ticket/{idTicket}")
    public ResponseEntity<?> getPiecesByTicket(@PathVariable Long idTicket) {
        try {
            logger.info("📋 GET /ticket/" + idTicket);
            List<PieceJointe> pieces = pieceJointeService.getPiecesByTicket(idTicket);

            Map<String, Object> response = new HashMap<>();
            response.put("total", pieces.size());
            response.put("pieces", pieces);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur récupération pièces: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 👤 RÉCUPÉRER LES PIÈCES D'UN UTILISATEUR
     */
    @GetMapping("/utilisateur/{idUtilisateur}")
    public ResponseEntity<?> getPiecesByUtilisateur(@PathVariable Long idUtilisateur) {
        try {
            logger.info("📋 GET /utilisateur/" + idUtilisateur);
            List<PieceJointe> pieces = pieceJointeService.getPiecesByUtilisateur(idUtilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("total", pieces.size());
            response.put("pieces", pieces);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur récupération pièces utilisateur: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 🗑️ SUPPRIMER UNE PIÈCE JOINTE
     */
    @DeleteMapping("/{idPieceJointe}")
    public ResponseEntity<?> supprimerPiece(@PathVariable Long idPieceJointe) {
        try {
            logger.info("🗑️ DELETE /" + idPieceJointe);
            pieceJointeService.supprimerPieceJointe(idPieceJointe);

            Map<String, String> response = new HashMap<>();
            response.put("message", "✅ Pièce jointe supprimée");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur suppression pièce: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 🗑️ SUPPRIMER TOUTES LES PIÈCES D'UN TICKET
     */
    @DeleteMapping("/ticket/{idTicket}/all")
    public ResponseEntity<?> supprimerPiecesByTicket(@PathVariable Long idTicket) {
        try {
            logger.info("🗑️ DELETE /ticket/" + idTicket + "/all");
            pieceJointeService.supprimerPiecesByTicket(idTicket);

            Map<String, String> response = new HashMap<>();
            response.put("message", "✅ Toutes les pièces jointes supprimées");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erreur suppression pièces ticket: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 📥 TÉLÉCHARGER UN FICHIER
     */
    @GetMapping("/download/{idPieceJointe}")
    public ResponseEntity<?> telechargerFichier(@PathVariable Long idPieceJointe) {
        try {
            logger.info("📥 GET /download/" + idPieceJointe);

            // ✅ Récupérer la pièce jointe
            Optional<PieceJointe> pieceOpt = pieceJointeService.getPieceJointeById(idPieceJointe);

            if (pieceOpt.isEmpty()) {
                logger.error("❌ Pièce jointe non trouvée: " + idPieceJointe);
                return ResponseEntity.notFound().build();
            }

            PieceJointe piece = pieceOpt.get();
            logger.info("✅ Pièce trouvée: " + piece.getNomJointe() + " - Chemin: " + piece.getCheminStockage());

            // ✅ Vérifier que le fichier existe
            File file = new File(piece.getCheminStockage());
            if (!file.exists()) {
                logger.error("❌ Fichier non trouvé sur le disque: " + piece.getCheminStockage());
                return ResponseEntity.status(404)
                        .body("❌ Fichier non trouvé sur le serveur");
            }

            logger.info("✅ Fichier trouvé, taille: " + file.length() + " bytes");

            // ✅ Créer la ressource
            Resource resource = new FileSystemResource(file);

            // ✅ Retourner le fichier avec les bons headers
            return ResponseEntity.ok()
                    .contentType(getMediaType(piece.getTypefichier()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + piece.getNomJointe() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .body(resource);

        } catch (Exception e) {
            logger.error("❌ Erreur téléchargement Fichier non trouvée " );
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("❌ Erreur: " );
        }
    }

    /**
     * 🔍 Obtenir le type MIME
     */
    private MediaType getMediaType(String extension) {
        if (extension == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        return switch (extension.toLowerCase()) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "txt" -> MediaType.TEXT_PLAIN;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "doc" -> MediaType.valueOf("application/msword");
            case "docx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xls" -> MediaType.valueOf("application/vnd.ms-excel");
            case "xlsx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "zip" -> MediaType.valueOf("application/zip");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}