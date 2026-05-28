package com.sagem.g2ii.Service;

import com.sagem.g2ii.Entity.Inventaire.ConsommationPiece;
import java.util.List;

public interface IConsommationPieceService {
    ConsommationPiece enregistrerConsommation(ConsommationPiece consommation);
    List<ConsommationPiece> obtenirConsommationsParTicket(String referenceTicket);
    List<ConsommationPiece> obtenirToutesLesConsommations();
    void annulerConsommation(Long id);
}