package com.sagem.g2ii.Entity.Enumeration;

public enum ActionAudit {

    // =========================================================================
    // 🔐 AUTHENTIFICATION & SÉCURITÉ
    // =========================================================================
    CONNEXION,
    DECONNEXION,
    ECHEC_CONNEXION,
    BLOCAGE,
    DEBLOCAGE,
    CHANGEMENT_MDP,
    RESET_MDP,

    // Équivalents Anglais (si conservés pour compatibilité d'anciens modules)
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_CHANGED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,

    // =========================================================================
    // 👤 GESTION DES UTILISATEURS & DROITS (Module UTILISATEUR)
    // =========================================================================
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    CREATE_GROUP,       // 🌟 Pour GroupeService.ajouterGroup
    DELETE_GROUP,       // 🌟 Pour GroupeService.deleteGroupe

    // =========================================================================
    // 🎫 GESTION DES TICKETS (Module TICKET)
    // =========================================================================
    CREATE_TICKET,
    UPDATE_TICKET,
    DELETE_TICKET,
    ASSIGN_TICKET,
    CLOSE_TICKET,
    REOPEN_TICKET,

    // =========================================================================
    // 📦 MAGASIN, CATALOGUE & STOCKS (Module STOCK)
    // =========================================================================
    CREATE_MATERIEL,    // Création de la fiche article catalogue
    UPDATE_MATERIEL,    // Modification article catalogue
    DELETE_MATERIEL,    // Suppression / Archivage article

    CREATE_STOCK,       // 🌟 Pour StockService.creerStock
    UPDATE_STOCK,       // 🌟 Pour l'ajustement des quantités / inventaires
    DELETE_STOCK,       // 🌟 Pour le retrait d'un rayon de stock

    STOCK_IN,           // 🌟 Pour l'annulation / réintégration de pièces (ConsommationPiece.annuler)
    STOCK_OUT,          // 🌟 Pour la consommation de pièces (ConsommationPiece.enregistrer)

    CREATE_FOURNISSEUR, // 🌟 Pour FournisseurService.createFournisseur
    UPDATE_FOURNISSEUR, // 🌟 Pour FournisseurService.updateFournisseur
    DELETE_FOURNISSEUR, // 🌟 Pour FournisseurService.deleteFournisseur

    CREATE_LOCALISATION,// 🌟 Pour LocalisationMatrielService.creerLocalisation
    UPDATE_LOCALISATION,// 🌟 Pour LocalisationMatrielService.modifierLocalisation
    DELETE_LOCALISATION,// 🌟 Pour LocalisationMatrielService.supprimerLocalisation

    // =========================================================================
    // 🛒 DEMANDES D'ACHATS (Module DEMANDE_ACHAT)
    // =========================================================================
    CREATE_REQUEST,
    APPROVE_REQUEST,
    REJECT_REQUEST,
    APPROBATION_DEMANDE, // Traitement générique de validation

    // =========================================================================
    // ⚙️ PARAMÉTRAGE SYSTÈME (Module GESTION_APPLICATION)
    // =========================================================================
    UPDATE_CONFIGURATION, // 🌟 Pour ConfigurationGlobaleService (Évite d'utiliser CHANGEMENT_MDP)

    // =========================================================================
    // 📊 EXTRACTIONS & COMMUNICATIONS
    // =========================================================================
    EXPORT_PDF,
    EXPORT_EXCEL,
    EXPORT_CSV,
    SEND_EMAIL
}