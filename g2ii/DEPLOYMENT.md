# G2II - DEPLOYMENT AVEC DOCKER + PostgreSQL

## 🐳 Étape 1: Compiler le JAR

**IMPORTANT:** Vous DEVEZ compiler le JAR avant de builder l'image Docker!

```powershell
cd "d:\Test pfe 2026\g2ii"
mvn clean package -DskipTests
```

**Résultat attendu:** `target/g2ii-0.0.1-SNAPSHOT.jar` ✅

---

## 🏗️ Étape 2: Build l'Image Docker

```powershell
docker build -t g2ii-backend-app:latest .
```

**Vérifiez que l'image est créée:**
```powershell
docker images | findstr "g2ii"
```

---

## 🚀 Étape 3: Lancer les Conteneurs

### **Option A: Avec Docker Compose (RECOMMANDÉ)**

```powershell
docker-compose up -d
```

Cela démarre:
- ✅ **PostgreSQL** sur port `5432` (accès: `sameh:sameh`)
- ✅ **Backend** sur port `8082`

**Vérifiez l'état:**
```powershell
docker-compose ps
```

**Logs de l'application:**
```powershell
docker-compose logs -f backend-app
```

**Arrêter tout:**
```powershell
docker-compose down
```

---

### **Option B: Manuellement (Sans Docker Compose)**

#### 1️⃣ Démarrer PostgreSQL
```powershell
docker run -d `
  --name ma-base-g2ii `
  -e POSTGRES_DB=ouechtati `
  -e POSTGRES_USER=sameh `
  -e POSTGRES_PASSWORD=sameh `
  -p 5432:5432 `
  postgres:latest
```

#### 2️⃣ Attendre que PostgreSQL soit prêt (±10 secondes)
```powershell
Start-Sleep -Seconds 10
```

#### 3️⃣ Démarrer l'Application
```powershell
docker run -d `
  --name g2ii-app-container `
  --link ma-base-g2ii:ma-base-g2ii `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://ma-base-g2ii:5432/ouechtati `
  -e SPRING_DATASOURCE_USERNAME=sameh `
  -e SPRING_DATASOURCE_PASSWORD=sameh `
  -e SPRING_PROFILES_ACTIVE=prod `
  -p 8082:8082 `
  g2ii-backend-app:latest
```

---

## 📊 Vérifier que Tout Fonctionne

### **1️⃣ Tester l'Endpoint**
```powershell
curl -X POST "http://localhost:8082/api/login" `
  -H "Content-Type: application/json" `
  -d '{
    "email": "admin@test.local",
    "motDePasse": "Admin123!"
  }'
```

### **2️⃣ Accéder à Swagger UI**
```
http://localhost:8082/swagger-ui.html
```

### **3️⃣ Se Connecter à PostgreSQL**
```powershell
docker exec -it ma-base-g2ii psql -U sameh -d ouechtati
```

**Commandes utiles dans psql:**
```sql
-- Lister les tables
\dt

-- Voir les utilisateurs
SELECT * FROM utilisateur;

-- Voir les demandes
SELECT * FROM demande_inscription;

-- Quitter
\q
```

---

## 🔧 Commandes Utiles

### **Voir les logs**
```powershell
docker logs -f g2ii-app-container
```

### **Arrêter les conteneurs**
```powershell
docker stop ma-base-g2ii g2ii-app-container
```

### **Supprimer les conteneurs**
```powershell
docker rm ma-base-g2ii g2ii-app-container
```

### **Supprimer tout (conteneurs + images)**
```powershell
docker-compose down --rmi all
```

---

## 📋 Architecture Docker

```
┌─────────────────────────────────────┐
│  Votre Machine (Windows)             │
│  :8082                                │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  Docker Network (bridge)              │
│                                      │
│  ┌──────────────┐  ┌──────────────┐  │
│  │ PostgreSQL   │  │ Backend G2II │  │
│  │ :5432        │  │ :8082        │  │
│  └──────────────┘  └──────────────┘  │
│     (sameh:sameh)  (Spring Boot)     │
└─────────────────────────────────────┘
```

---

## ⚙️ Variables d'Environnement

| Variable | Dev | Prod |
|----------|-----|------|
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:testdb` | `jdbc:postgresql://ma-base-g2ii:5432/ouechtati` |
| `SPRING_DATASOURCE_USERNAME` | `sa` | `sameh` |
| `SPRING_DATASOURCE_PASSWORD` | `` | `sameh` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `create-drop` | `update` |
| `SPRING_PROFILES_ACTIVE` | `dev` | `prod` |

---

## 🐛 Troubleshooting

### **❌ Erreur: "Cannot connect to PostgreSQL"**
```powershell
# Attendez quelques secondes que PostgreSQL démarre
docker logs ma-base-g2ii
Start-Sleep -Seconds 15
docker-compose up -d
```

### **❌ Port déjà utilisé (8082)**
```powershell
# Trouver le processus qui utilise le port
netstat -ano | findstr :8082

# Ou utiliser un autre port
docker run -p 9000:8082 g2ii-backend-app:latest
```

### **❌ Docker image not found**
```powershell
# Vérifiez que vous avez compilé le JAR
mvn clean package -DskipTests

# Rebuildez l'image
docker build -t g2ii-backend-app:latest .
```

---

## ✅ Checklist Déploiement

- [ ] Maven compilé: `mvn clean package -DskipTests`
- [ ] JAR créé: `target/g2ii-0.0.1-SNAPSHOT.jar`
- [ ] Image buildée: `docker build -t g2ii-backend-app:latest .`
- [ ] Docker Compose prêt: `docker-compose up -d`
- [ ] PostgreSQL santé: `docker-compose ps` (healthy ✓)
- [ ] Backend démarré: Vérifier logs
- [ ] Swagger accessible: `http://localhost:8082/swagger-ui.html`
- [ ] Login fonctionne: Tester `/api/login`

---
