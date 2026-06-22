//package com.sagem.g2ii.Entity.Inventaire;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "configuration_seuils")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class ConfigurationSeuils {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private Integer seuilMinimumDefaut = 5;
//
//    @Column(nullable = false)
//    private Integer seuilCritiqueDefaut = 2;
//
//    @Column(nullable = false, length = 50)
//    private String methodeValuationDefaut = "CMP";  // FIFO, LIFO, CMP
//
//    @Column(name = "taux_depreciation_defaut")
//    private Double tauxDepreciationDefaut;  // 20% par an
//
//    @Column(nullable = false)
//    private Integer delaiAvertissement = 7;         // Jours avant expiration
//
//    @Column(nullable = false)
//    private Boolean autoriserStockNegatif = false;
//
//    @Column(nullable = false)
//    private Boolean genererAlerteAutomatique = true;
//
//    @Column(nullable = false)
//    private Boolean demandeConfirmationSortie = true;
//
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime dateCreation;
//
//    @Column(nullable = false)
//    private LocalDateTime dateModification;
//
//    @Column(length = 100, nullable = false)
//    private String modifiePar;
//
//    @PrePersist
//    protected void onCreate() {
//        dateCreation = LocalDateTime.now();
//        dateModification = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        dateModification = LocalDateTime.now();
//    }
//}
