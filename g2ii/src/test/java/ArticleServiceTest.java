//
//import com.sagem.g2ii.DTOs.ArticleDTO;
//import com.sagem.g2ii.Entity.Enumeration.StatutArticle;
//import com.sagem.g2ii.Entity.Enumeration.TypeArticle;
//import com.sagem.g2ii.Entity.Inventaire.Article;
//import com.sagem.g2ii.Entity.Inventaire.Fournisseur;
//import com.sagem.g2ii.Entity.Inventaire.Localisation;
//import com.sagem.g2ii.Repository.ArticleRepository;
//import com.sagem.g2ii.Repository.FournisseurRepository;
//import com.sagem.g2ii.Repository.LocalisationRepository;
//import com.sagem.g2ii.Service.AlerteService;
//import com.sagem.g2ii.Service.ArticleService;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class ArticleServiceTest {
//
//    @Mock
//    private ArticleRepository articleRepository;
//
//    @Mock
//    private LocalisationRepository localisationRepository;
//
//    @Mock
//    private FournisseurRepository fournisseurRepository;
//
//    @Mock
//    private AlerteService alerteService;
//
//    @InjectMocks
//    private ArticleService articleService;
//
//    private ArticleDTO articleDTO;
//    private Localisation localisationMock;
//    private Fournisseur fournisseurMock;
//    private Article articleSauvegarde;
//
//    @BeforeEach
//    void setUp() {
//        // Préparation d'un DTO valide
//        articleDTO = ArticleDTO.builder()
//                .categorie("Consommable")
//                .reference("REF-2026-XYZ")
//                .designation("Câble Fibre Optique")
//                .description("Câble sagemcom réseau")
//                .codeBarres("123456789")
//                .typeArticle(TypeArticle.CONSOMMABLE) // Adapte selon tes labels réels d'Enum
//                .statut(StatutArticle.ACTIF)  // Adapte selon tes labels réels d'Enum
//                .quantiteEnStock(1)                // Inférieur au seuil critique par défaut (2) pour tester l'alerte
//                .prixUnitaire(new BigDecimal("15.50"))
//                .seuilMinimum(5)
//                .seuilCritique(2)
//                .localisationId(10L)
//                .fournisseurId(20L)
//                .build();
//
//        // Préparation des entités liées simulées (Mocks)
//        localisationMock = new Localisation();
//        localisationMock.setId(10L);
//        localisationMock.setBatiment("Bâtiment A");
//        localisationMock.setBureau("Bureau 102");
//
//        fournisseurMock = new Fournisseur();
//        fournisseurMock.setId(20L);
//        fournisseurMock.setNom("Sagemcom Fournisseur Officiel");
//
//        // Objet Entité attendu après sauvegarde
//        articleSauvegarde = Article.builder()
//                .id(1L)
//                .categorie("Consommable")
//                .reference("REF-2026-XYZ")
//                .designation("Câble Fibre Optique")
//                .quantiteEnStock(1)
//                .seuilMinimum(5)
//                .seuilCritique(2)
//                .prixUnitaire(new BigDecimal("15.50"))
//                .localisation(localisationMock)
//                .fournisseur(fournisseurMock)
//                .build();
//    }
//
//    @Test
//    void creerArticle_DevraitEnregistrerEtRetournerLeDTO_AvecAlertes() {
//        // 1. ARRANGEMENT
//        when(localisationRepository.findById(10L)).thenReturn(Optional.of(localisationMock));
//        when(fournisseurRepository.findById(20L)).thenReturn(Optional.of(fournisseurMock));
//        when(articleRepository.save(any(Article.class))).thenReturn(articleSauvegarde);
//
//        // 2. ACT
//        ArticleDTO resultat = articleService.creerArticle(articleDTO);
//
//        // 3. ASSERTIONS
//        assertNotNull(resultat);
//        assertEquals(1L, resultat.getId());
//        assertEquals("REF-2026-XYZ", resultat.getReference());
//        assertEquals(10L, resultat.getLocalisationId());
//        assertEquals(20L, resultat.getFournisseurId());
//        assertEquals("Sagemcom Fournisseur Officiel", resultat.getFournisseurNom());
//
//        // Capturer l'entité construite pour vérifier les liaisons
//        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
//        verify(articleRepository, times(1)).save(articleCaptor.capture());
//
//        Article articleGenere = articleCaptor.getValue();
//        assertEquals("Consommable", articleGenere.getCategorie());
//        assertEquals("REF-2026-XYZ", articleGenere.getReference());
//
//        // Vérifier si la méthode d'alerte a bien été appelée car la quantité (1) <= Seuil critique (2)
//        verify(alerteService, times(1)).creerAlerte(eq(articleSauvegarde), contains("Stock critique"));
//    }
//
//    @Test
//    void creerArticle_DevraitLeverException_SiCategorieManquante() {
//        // ARRANGEMENT
//        articleDTO.setCategorie(null);
//
//        // ACT & ASSERT
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            articleService.creerArticle(articleDTO);
//        });
//
//        assertEquals("La catégorie de l'article est obligatoire.", exception.getMessage());
//        verify(articleRepository, never()).save(any());
//    }
//
//    @Test
//    void creerArticle_DevraitLeverException_SiLocalisationIntrouvable() {
//        // ARRANGEMENT
//        when(localisationRepository.findById(10L)).thenReturn(Optional.empty());
//
//        // ACT & ASSERT
//        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
//            articleService.creerArticle(articleDTO);
//        });
//
//        assertTrue(exception.getMessage().contains("Localisation introuvable"));
//        verify(articleRepository, never()).save(any());
//    }
//
//    @Test
//    void getArticleById_DevraitRetournerArticle_SiIdExiste() {
//        // ARRANGEMENT
//        when(articleRepository.findById(1L)).thenReturn(Optional.of(articleSauvegarde));
//
//        // ACT
//        ArticleDTO resultat = articleService.getArticleById(1L);
//
//        // ASSERT
//        assertNotNull(resultat);
//        assertEquals(1L, resultat.getId());
//        verify(articleRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void getArticleById_DevraitLeverException_SiIdNExistePas() {
//        // ARRANGEMENT
//        when(articleRepository.findById(99L)).thenReturn(Optional.empty());
//
//        // ACT & ASSERT
//        assertThrows(EntityNotFoundException.class, () -> {
//            articleService.getArticleById(99L);
//        });
//    }
//
//    @Test
//    void archiveArticle_DevraitPasserLeStatutAArchive() {
//        // ARRANGEMENT
//        when(articleRepository.findById(1L)).thenReturn(Optional.of(articleSauvegarde));
//
//        // ACT
//        articleService.archiveArticle(1L);
//
//        // ASSERT
//        assertEquals(StatutArticle.ARCHIVÉ, articleSauvegarde.getStatut());
//        verify(articleRepository, times(1)).save(articleSauvegarde);
//    }
//}