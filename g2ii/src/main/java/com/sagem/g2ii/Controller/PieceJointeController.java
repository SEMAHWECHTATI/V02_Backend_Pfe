package com.sagem.g2ii.Controller;

import com.sagem.g2ii.Entity.Intervention.PieceJointe;
import com.sagem.g2ii.Service.PieceJointeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/fichiers")
@CrossOrigin("*")
public class PieceJointeController {

    @Autowired
    private PieceJointeService pieceJointeService;

    @PostMapping("/upload/ticket/{idTicket}")
    public ResponseEntity<?> uploaderFichier(@PathVariable Long idTicket, @RequestParam("file") MultipartFile fichier) {
        try {
            PieceJointe pj = pieceJointeService.uploaderFichier(idTicket, fichier);
            return ResponseEntity.ok(pj);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur d'upload : " + e.getMessage());
        }
    }
}