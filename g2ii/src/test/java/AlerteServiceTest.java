//
//
//import com.sagem.g2ii.DTOs.AlerteDTO;
//import com.sagem.g2ii.Entity.Authentification.PreferenceNotification;
//import com.sagem.g2ii.Entity.Authentification.Utilisateur;
//import com.sagem.g2ii.Entity.Enumeration.*;
//import com.sagem.g2ii.Entity.Inventaire.Alerte;
//import com.sagem.g2ii.Entity.Inventaire.Article;
//import com.sagem.g2ii.Repository.AlerteRepository;
//import com.sagem.g2ii.Repository.IntUtilisateur;
//import com.sagem.g2ii.Repository.NotificationRepository;
//import com.sagem.g2ii.Repository.PreferenceNotificationRepository;
//import com.sagem.g2ii.Service.AlerteService;
//import com.sagem.g2ii.Service.EmailService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AlerteServiceTest {
//
//    @Mock
//    private AlerteRepository alerteRepository;
//
//    @Mock
//    private NotificationRepository notificationRepository;
//
//    @Mock
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private PreferenceNotificationRepository preferenceRepository;
//
//    @Mock
//    private IntUtilisateur utilisateurRepository;
//
//    @InjectMocks
//    private AlerteService alerteService;
//
//    private Article articleTest;
//    private Utilisateur adminMock;
//    private PreferenceNotification preferenceMock;
//    private Alerte alerteSimulee;
//
//    @BeforeEach
//    void setUp() {
//        // 1. Initialisation de l'article pour le test
//        articleTest = Article.builder()
//                .id(100L)
//                .designation("Routeur Sagemcom VDSL")
//                .quantiteEnStock(0) // Provoquera une rupture de stock -> Sévérité CRITIQUE
//                .seuilCritique(2)
//                .seuilMinimum(5)
//                .build();
//
//        // 2. Initialisation d'un destinataire (Admin)
//        adminMock = Utilisateur.builder()
//                .id(1L)
//                .email("admin.test@sagem.com")
//                .role(roleUtilisateur.Administrateur)
//                .build();
//
//        // 3. Configuration des préférences de notification (Tout activé)
//        preferenceMock = PreferenceNotification.builder()
//                .id(5L)
//                .actif(true)
//                .canalInApp(true)
//                .canalEmail(true)
//                .build();
//
//        // 4. Objet Alerte qui sera retourné après persistance
//        alerteSimulee = Alerte.builder()
//                .id(1L)
//                .type(TypeAlerte.RUPTURE_STOCK)
//                .message("Stock épuisé !")
//                .severite(Severite.CRITIQUE)
//                .statut(StatutAlerte.NOUVELLE)
//                .articles(new ArrayList<>(Collections.singletonList(articleTest)))
//                .build();
//    }
//
//    @Test
//    void creerAlerte_RuptureStock_DevraitEscaladerAuxAdmins_EtNotifierSelonPreferences() {
//        // ARRANGEMENT
//        // Sévérité CRITIQUE -> va chercher les Administrateurs
//        when(utilisateurRepository.findByRole(roleUtilisateur.Administrateur))
//                .thenReturn(Collections.singletonList(adminMock));
//
//        // Mock de la sauvegarde en base
//        when(alerteRepository.save(any(Alerte.class))).thenReturn(alerteSimulee);
//
//        // Mock du chargement des préférences de l'utilisateur ID 1
//        when(preferenceRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(preferenceMock));
//
//        // ACT
//        AlerteDTO resultat = alerteService.creerAlerte(articleTest, "Stock épuisé !");
//
//        // ASSERTIONS
//        assertNotNull(resultat);
//        assertEquals(1L, resultat.getId());
//        assertEquals(Severite.CRITIQUE, resultat.getSeverite());
//        assertEquals(TypeAlerte.RUPTURE_STOCK, resultat.getType());
//
//        // VÉRIFICATIONS DES CANAUX DE DIFFUSION
//        // 1. Vérification WebSocket STOMP (Angular)
//        verify(messagingTemplate, times(1))
//                .convertAndSend(eq("/topic/inventory-alertes"), any(AlerteDTO.class));
//
//        // 2. Vérification E-mail (Sévérité critique + préférence e-mail à true)
//        verify(emailService, times(1))
//                .envoyerEmailAlerteSysteme(eq("admin.test@sagem.com"), anyString(), eq("Stock épuisé !"));
//
//        // 3. Vérification de la persistance de l'historique d'audit
//        verify(notificationRepository, times(1)).saveAll(anyList());
//    }
//
//    @Test
//    void creerAlerte_NeDevraitPasEnvoyerEmail_SiPreferenceCanalEmailEstFausse() {
//        // ARRANGEMENT : L'utilisateur coupe le canal E-mail
//        preferenceMock.setCanalEmail(false);
//
//        when(utilisateurRepository.findByRole(roleUtilisateur.Administrateur))
//                .thenReturn(Collections.singletonList(adminMock));
//        when(alerteRepository.save(any(Alerte.class))).thenReturn(alerteSimulee);
//        when(preferenceRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(preferenceMock));
//
//        // ACT
//        alerteService.creerAlerte(articleTest, "Stock épuisé !");
//
//        // ASSERTIONS & VÉRIFICATIONS
//        // Le WebSocket doit toujours partir
//        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/inventory-alertes"), any(AlerteDTO.class));
//
//        // L'e-mail ne doit JAMAIS être appelé car désactivé dans ses préférences
//        verify(emailService, never()).envoyerEmailAlerteSysteme(anyString(), anyString(), anyString());
//    }
//
//    @Test
//    void creerAlerte_NeDevraitRienDistribuer_SiNotificationsGlobalementDesactivees() {
//        // ARRANGEMENT : L'utilisateur désactive globalement ses notifications (.setActif(false))
//        preferenceMock.setActif(false);
//
//        when(utilisateurRepository.findByRole(roleUtilisateur.Administrateur))
//                .thenReturn(Collections.singletonList(adminMock));
//        when(alerteRepository.save(any(Alerte.class))).thenReturn(alerteSimulee);
//        when(preferenceRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(preferenceMock));
//
//        // ACT
//        alerteService.creerAlerte(articleTest, "Stock épuisé !");
//
//        // VÉRIFICATIONS
//        // Si les notifications sont inactives, aucun traitement de distribution ne doit avoir lieu
//        verify(messagingTemplate, never()).convertAndSend(anyString(), any(AlerteDTO.class));
//        verify(emailService, never()).envoyerEmailAlerteSysteme(anyString(), anyString(), anyString());
//        verify(notificationRepository, never()).saveAll(anyList());
//    }
//
//    @Test
//    void marquerCommeLue_DevraitChangerLeStatut() {
//        // ARRANGEMENT
//        when(alerteRepository.findById(1L)).thenReturn(Optional.of(alerteSimulee));
//
//        // ACT
//        alerteService.marquerCommeLue(1L);
//
//        // ASSERT
//        assertEquals(StatutAlerte.LUE, alerteSimulee.getStatut());
//        verify(alerteRepository, times(1)).save(alerteSimulee);
//    }
//}