//
//
//import com.sagem.g2ii.DTOs.TicketCreationDTO;
//import com.sagem.g2ii.DTOs.TicketStatisticsDTO;
//import com.sagem.g2ii.Entity.Authentification.Groupe;
//import com.sagem.g2ii.Entity.Authentification.Utilisateur;
//import com.sagem.g2ii.Entity.Enumeration.GroupeTechnicien;
//import com.sagem.g2ii.Entity.Enumeration.Priorite;
//import com.sagem.g2ii.Entity.Enumeration.StatutTicket;
//import com.sagem.g2ii.Entity.Intervention.Categorie;
//import com.sagem.g2ii.Entity.Intervention.SLA;
//import com.sagem.g2ii.Entity.Intervention.Ticket;
//import com.sagem.g2ii.Repository.*;
//import com.sagem.g2ii.Service.HistoriqueTicketService;
//import com.sagem.g2ii.Service.TicketService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class TicketServiceTest {
//
//    @Mock
//    private TicketRepo ticketRepository;
//
//    @Mock
//    private CategorieRepo categorieRepository;
//
//    @Mock
//    private IntUtilisateur utilisateurRepository;
//
//    @Mock
//    private IntGroupe groupeRepository;
//
//    @Mock
//    private SLARepository slaRepository;
//
//    @Mock
//    private HistoriqueTicketService historiqueService;
//
//    @InjectMocks
//    private TicketService ticketService;
//
//    private TicketCreationDTO ticketCreationDTO;
//    private Utilisateur demandeurMock;
//    private Utilisateur technicienMock;
//    private Categorie categorieMock;
//    private Groupe groupeMock;
//    private SLA slaMock;
//    private Ticket ticketMock;
//
//    @BeforeEach
//    void setUp() {
//        // 1. Préparation du DTO d'entrée
//        ticketCreationDTO = new TicketCreationDTO();
//        ticketCreationDTO.setTitre("Panne Réseau Fibre");
//        ticketCreationDTO.setDescription("Coupure totale de la liaison switch principal");
//        ticketCreationDTO.setDemandeurId(1L);
//        ticketCreationDTO.setCategorieId(2L);
//        ticketCreationDTO.setGroupeId(3L);
//        ticketCreationDTO.setPriorite(Priorite.Haute);
//
//        // 2. Préparation des Mocks d'entités de base
//        demandeurMock = Utilisateur.builder().id(1L).email("demandeur@sagem.com").build();
//        technicienMock = Utilisateur.builder().id(5L).email("tech.network@sagem.com").build();
//
//        categorieMock = new Categorie();
//        categorieMock.setIdCategorie(2L);
//        categorieMock.setNomCategorie("Réseau & Télécom");
//
//        groupeMock = new Groupe();
//        groupeMock.setId(3L);
//        groupeMock.setNomGroupes(GroupeTechnicien.IT_Maintenance_Informatique);
//
//        // 3. Contrat SLA (Exemple: Haute Priorité -> Prise en charge 2h / Résolution 4h)
//        slaMock = new SLA();
//        slaMock.setIdSLA(10L);
//        slaMock.setNomSLA("SLA Réseau Haute");
//        slaMock.setDelaiPriseEnChargeHeure(2);
//        slaMock.setDelaiResolutionHeure(4);
//
//        // 4. Entité Ticket simulée après enregistrement initial
//        ticketMock = new Ticket();
//        ticketMock.setIdTicket(500L);
//        ticketMock.setTitre("Panne Réseau Fibre");
//        ticketMock.setDescription("Coupure totale de la liaison switch principal");
//        ticketMock.setStatut(StatutTicket.Nouveau);
//        ticketMock.setDate(LocalDateTime.now().minusHours(3)); // Créé il y a 3 heures pour forcer les calculs
//        ticketMock.setSlaAssigne(slaMock);
//        ticketMock.setDemandeur(demandeurMock);
//    }
//
//    // ============================================================================
//    // 🧪 TESTS SUR LA CRÉATION ET L'ASSIGNATION DU SLA
//    // ============================================================================
//
//    @Test
//    void creerTicket_DevraitAssignerSlaEtGenererReference_LorsqueDonneesValides() {
//        // ARRANGEMENT
//        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(demandeurMock));
//        when(categorieRepository.findById(2L)).thenReturn(Optional.of(categorieMock));
//        when(groupeRepository.findById(3L)).thenReturn(Optional.of(groupeMock));
//        when(slaRepository.findByCategorieAndPriorite(categorieMock, Priorite.Haute)).thenReturn(slaMock);
//
//        // Simuler le fait qu'il n'y a pas d'autre ticket créé aujourd'hui pour le compteur de référence
//        when(ticketRepository.findTopByReferenceStartingWithOrderByReferenceDesc(anyString())).thenReturn(Optional.empty());
//
//        // Retourner l'entité sauvegardée
//        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // ACT
//        Ticket resultat = ticketService.creerTicket(ticketCreationDTO);
//
//        // ASSERTIONS
//        assertNotNull(resultat);
//        assertEquals(StatutTicket.Nouveau, resultat.getStatut());
//        assertEquals("Panne Réseau Fibre", resultat.getTitre());
//        assertEquals(slaMock, resultat.getSlaAssigne());
//        assertTrue(resultat.getReference().startsWith("SAG"));
//        assertTrue(resultat.getReference().endsWith("-0001")); // Premier compteur
//        verify(ticketRepository, times(1)).save(any(Ticket.class));
//    }
//
//    @Test
//    void creerTicket_DevraitLeverException_SiTitreManquant() {
//        // ARRANGEMENT
//        ticketCreationDTO.setTitre("");
//
//        // ACT & ASSERT
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            ticketService.creerTicket(ticketCreationDTO);
//        });
//
//        assertEquals("Le titre est requis", exception.getMessage());
//        verify(ticketRepository, never()).save(any());
//    }
//
//    // ============================================================================
//    // 🧪 TESTS SUR LE CYCLE DE VIE ET LES DÉPASSEMENTS SLA
//    // ============================================================================
//
//    @Test
//    void demarrerTicket_DevraitCalculerSlaPriseEnCharge_EtDetecterDepassement_SiTropTard() {
//        // ARRANGEMENT
//        // Le ticket a été créé il y a 3 heures (voir setUp). Le délai max autorisé est de 2 heures.
//        // 3h écoulées > 2h max => Le SLA de prise en charge doit être violé (false).
//        when(ticketRepository.findById(500L)).thenReturn(Optional.of(ticketMock));
//        when(utilisateurRepository.findById(5L)).thenReturn(Optional.of(technicienMock));
//        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // ACT
//        Ticket resultat = ticketService.demarrerTicket(500L, 5L);
//
//        // ASSERTIONS
//        assertEquals(StatutTicket.En_Cours, resultat.getStatut());
//        assertEquals(technicienMock, resultat.getTechnicienAssigne());
//        assertFalse(resultat.getSlaPriseEnChargeRespecte()); // ❌ Dépassé !
//        assertFalse(resultat.getSlaRespecte()); // Le SLA global passe à faux
//
//        // Vérification de la traçabilité d'audit
//        verify(historiqueService, times(1)).tracer(eq(ticketMock), eq(technicienMock), eq("Statut"), anyString(), anyString());
//    }
//
//    @Test
//    void resoudreTicket_DevraitCalculerSlaResolution_EtEnregistrerNote() {
//        // ARRANGEMENT
//        ticketMock.setStatut(StatutTicket.En_Cours);
//        ticketMock.setSlaPriseEnChargeRespecte(true); // Imaginons que la prise en charge était OK
//
//        // On force la date de création à il y a 5 heures.
//        // Résolution immédiate -> 5h écoulées > 4h max (SLA) => Devrait invalider le SLA de résolution.
//        ticketMock.setDate(LocalDateTime.now().minusHours(5));
//
//        when(ticketRepository.findById(500L)).thenReturn(Optional.of(ticketMock));
//        when(utilisateurRepository.findById(5L)).thenReturn(Optional.of(technicienMock));
//        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // ACT
//        Ticket resultat = ticketService.resoudreTicket(500L, 5L, "Remplacement du cordon JAR", 300);
//
//        // ASSERTIONS
//        assertEquals(StatutTicket.Resolu, resultat.getStatut());
//        assertEquals("Remplacement du cordon JAR", resultat.getNoteResolution());
//        assertEquals(Double.valueOf(300), resultat.getDelaiResolution());
//        assertFalse(resultat.getSlaResolutionRespecte()); // ❌ Dépassé (5h au lieu de 4h)
//        assertFalse(resultat.getSlaRespecte()); // Global corrompu
//    }
//
//    @Test
//    void cloturerTicket_DevraitChangerLeStatut_SiUtilisateurEstLeDemandeur() {
//        // ARRANGEMENT
//        ticketMock.setStatut(StatutTicket.Resolu);
//
//        when(ticketRepository.findById(500L)).thenReturn(Optional.of(ticketMock));
//        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(demandeurMock));
//        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // ACT
//        Ticket resultat = ticketService.cloturerTicket(500L, 1L, "DEMANDEUR");
//
//        // ASSERTIONS
//        assertEquals(StatutTicket.Cloture, resultat.getStatut());
//        assertNotNull(resultat.getDateCloture());
//        verify(ticketRepository, times(1)).save(ticketMock);
//    }
//
//    @Test
//    void cloturerTicket_DevraitLeverException_SiTicketNonResolu() {
//        // ARRANGEMENT : Le ticket est encore Nouveau (Pas résolu)
//        ticketMock.setStatut(StatutTicket.Nouveau);
//
//        when(ticketRepository.findById(500L)).thenReturn(Optional.of(ticketMock));
//        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(demandeurMock));
//
//        // ACT & ASSERT
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            ticketService.cloturerTicket(500L, 1L, "DEMANDEUR");
//        });
//
//        assertTrue(exception.getMessage().contains("Le ticket doit être RESOLU avant clôture"));
//        verify(ticketRepository, never()).save(any());
//    }
//
//    // ============================================================================
//    // 🧪 TESTS DES STATISTIQUES
//    // ============================================================================
//
//    @Test
//    void obtenirStatistiques_DevraitRetournerLeKpiGlobal() {
//        // ARRANGEMENT
//        // Pour éviter de mocker chaque sous-méthode du repository, on renvoie une liste vide/mockée de findAll
//        when(ticketRepository.findAll()).thenReturn(java.util.Collections.emptyList());
//
//        // ACT
//        TicketStatisticsDTO stats = ticketService.obtenirStatistiques();
//
//        // ASSERTIONS
//        assertNotNull(stats);
//        assertEquals(0, stats.getTotalTickets());
//        verify(ticketRepository, times(1)).findAll();
//    }
//}