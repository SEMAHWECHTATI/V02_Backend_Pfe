//
//
//import com.sagem.g2ii.DTOs.ApprobationDTO;
//import com.sagem.g2ii.Entity.Authentification.DemandeInscription;
//import com.sagem.g2ii.Entity.Authentification.Utilisateur;
//import com.sagem.g2ii.Entity.Enumeration.roleUtilisateur;
//import com.sagem.g2ii.Entity.Enumeration.statutDemandeInscription;
//import com.sagem.g2ii.Entity.Enumeration.statutUtilisateur;
//
//import com.sagem.g2ii.Repository.IntUtilisateur;
//import com.sagem.g2ii.Repository.IntdemandeInscri;
//import com.sagem.g2ii.Repository.IntJournalAudit;  // 👈 AJOUTÉ
//import com.sagem.g2ii.Repository.IntReferenceNotif; // 👈 AJOUTÉ
//
//import com.sagem.g2ii.Service.DemandeService;
//import com.sagem.g2ii.Service.EmailService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class DemandeServiceTest {
//
//    @Mock
//    private IntdemandeInscri demandeRepo;
//
//    @Mock
//    private IntUtilisateur utilisateurRepo;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private IntJournalAudit auditRepo; // 👈 AJOUTÉ (Simule le dépôt d'audit)
//
//    @Mock
//    private IntReferenceNotif preferenceRepo; // 👈 AJOUTÉ (Simule les préférences de notification)
//
//    @InjectMocks
//    private DemandeService demandeService; // Mockito va injecter TOUS les mocks déclarés ci-dessus
//
//    private DemandeInscription demandeTest;
//    private ApprobationDTO approbationDTO;
//
//    @BeforeEach
//    void setUp() {
//        demandeTest = DemandeInscription.builder()
//                .id(2L)
//                .nom("Ouechtati")
//                .prenom("Sameh")
//                .email("sameh20950@gmail.com")
//                .matricule("S-557592")
//                .statut(statutDemandeInscription.En_Attente)
//                .build();
//
//        approbationDTO = new ApprobationDTO();
//        approbationDTO.setRoleAccorde(roleUtilisateur.Administrateur);
//        approbationDTO.setGroupeId(null);
//    }
//
//    @Test
//    void approuverDemande_DevraitCreerUtilisateurEtAccepterDemande() {
//        // 1. ARRANGEMENT
//        when(demandeRepo.findById(2L)).thenReturn(Optional.of(demandeTest));
//        when(passwordEncoder.encode(any(String.class))).thenReturn("fake_hashed_password");
//
//        // 2. ACT
//        demandeService.approuverDemande(2L, approbationDTO);
//
//        // 3. ASSERTIONS & VÉRIFICATIONS
//        assertEquals(statutDemandeInscription.ACCEPTEE, demandeTest.getStatut());
//        verify(demandeRepo, times(1)).save(demandeTest);
//
//        ArgumentCaptor<Utilisateur> utilisateurCaptor = ArgumentCaptor.forClass(Utilisateur.class);
//        verify(utilisateurRepo, times(1)).save(utilisateurCaptor.capture());
//
//        Utilisateur nouvelUtilisateur = utilisateurCaptor.getValue();
//
//        assertNotNull(nouvelUtilisateur);
//        assertEquals("sameh20950@gmail.com", nouvelUtilisateur.getEmail());
//        assertEquals(roleUtilisateur.Administrateur, nouvelUtilisateur.getRole());
//        assertEquals(statutUtilisateur.Actif, nouvelUtilisateur.getStatut());
//        assertEquals("fake_hashed_password", nouvelUtilisateur.getMotDePasse());
//        assertTrue(nouvelUtilisateur.isMotDepassetemporaire());
//
//        // Vérification de la persistance de l'audit et des préférences
//        verify(auditRepo, times(1)).save(any()); // 👈 AJOUTÉ
//        verify(preferenceRepo, times(1)).save(any()); // 👈 AJOUTÉ
//        verify(emailService, times(1)).envoyerEmailBienvenue(eq("sameh20950@gmail.com"), any(), any());
//    }
//
//    @Test
//    void approuverDemande_DevraitLeverException_SiDemandeIntrouvable() {
//        // ARRANGEMENT
//        when(demandeRepo.findById(99L)).thenReturn(Optional.empty());
//
//        // ACT & ASSERT
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            demandeService.approuverDemande(99L, approbationDTO);
//        });
//
//        assertEquals("Demande non trouvée", exception.getMessage());
//        verify(utilisateurRepo, never()).save(any());
//        verify(auditRepo, never()).save(any()); // 👈 AJOUTÉ (Sécurité : Pas d'audit si ça plante)
//    }
//}