# G2II - VERSION SIMPLE

## 🎯 Qu'est-ce qui a changé?

✅ **Suppressions:**
- ❌ JWT Token complexe
- ❌ Rate limiting
- ❌ JWT validation filter
- ❌ Custom authentication entry point

✅ **Simplifié:**
- ✅ SecurityConfig minimaliste (juste CORS + PasswordEncoder)
- ✅ AuthentificationController simple (juste login)
- ✅ Pas de tests complexes
- ✅ pom.xml allégé (pas de JJWT)

---

## 🚀 Lancer l'Application

### **Option 1: Avec Maven**
```bash
cd "d:\Test pfe 2026\g2ii"
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=simple"
```

### **Option 2: Compiler d'abord**
```bash
mvn clean package -DskipTests
java -jar target/g2ii-0.0.1-SNAPSHOT.jar --spring.profiles.active=simple
```

**L'app démarre sur:** `http://localhost:8082` ✅

---

## 📋 Endpoints Disponibles (Sans Authentification)

### **1️⃣ Login Simple**
```
POST http://localhost:8082/api/login
Content-Type: application/json

{
  "email": "test@gmail.com",
  "motDePasse": "password123"
}
```

**Réponse (200 OK):**
```json
{
  "email": "test@gmail.com",
  "nom": "Test",
  "prenom": "User",
  "role": "Demandeur",
  "message": "Connexion réussie!"
}
```

### **2️⃣ Créer une Demande**
```
POST http://localhost:8082/api/demandes/envoyer
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean@gmail.com",
  "matricule": "MAT001",
  "telephone": "0612345678",
  "departement": "IT",
  "roleDemande": "Demandeur",
  "motifDemande": "Test"
}
```

---

## 🧪 Lancer les Tests Simples

```bash
mvn clean test -Dtest=SimpleG2iiTest
```

---

## 📊 Fichiers Créés/Modifiés

| Fichier | Status |
|---------|--------|
| `SecurityConfigSimple.java` | ✅ Créé |
| `AuthentificationControllerSimple.java` | ✅ Créé |
| `SimpleG2iiTest.java` | ✅ Créé |
| `application-simple.yaml` | ✅ Créé |
| `pom.xml` | ✅ Modifié (JWT supprimé) |

---

## 🔗 Swagger UI

```
http://localhost:8082/swagger-ui.html
```

**Note:** Aucune authentification requise pour swagger!

---

## 💡 Comment Ajouter des Utilisateurs de Test?

Modifiez `TestDataInitializer.java` ou créez des utilisateurs via le formulaire "/api/demandes/envoyer"

---

**Besoin de revenir à la version complexe?**
Utilisez: `--spring.profiles.active=dev` ou ne spécifiez pas de profil
