package com.sagem.g2ii.Entity.Authentification;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Table(name = "groupes")
@AllArgsConstructor
@NoArgsConstructor
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private GroupeTechnicien nomGroupes;

    private String description;

    private boolean actif = true;

    private LocalDateTime dateCreation;

    @ManyToMany(mappedBy = "groupes")
    @JsonIgnore
    private List<Utilisateur> utilisateurs;


}
