#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Script de déploiement complet G2II avec Docker + PostgreSQL
.DESCRIPTION
    Compile le projet, crée les images Docker et démarre les conteneurs
.PARAMETER Action
    build   - Compile et crée les images Docker
    up      - Démarre les conteneurs (avec docker-compose)
    down    - Arrête les conteneurs
    logs    - Affiche les logs
    clean   - Supprime tout (conteneurs + images)
#>

param(
    [string]$Action = "up"
)

$ErrorActionPreference = "Stop"

function Write-Info { Write-Host "[INFO] $args" -ForegroundColor Green }
function Write-Error { Write-Host "[ERROR] $args" -ForegroundColor Red }
function Write-Warn { Write-Host "[WARN] $args" -ForegroundColor Yellow }

# =========================================================================
# 📦 BUILD
# =========================================================================
function Build-Project {
    Write-Info "🔨 Compilation du projet Maven..."
    mvn clean package -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build échoué!"
        exit 1
    }
    Write-Info "✅ Maven build réussi!"
    
    Write-Info "🐳 Build l'image Docker..."
    docker build -t g2ii-backend-app:latest .
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Docker build échoué!"
        exit 1
    }
    Write-Info "✅ Image Docker créée!"
}

# =========================================================================
# 🚀 UP
# =========================================================================
function Start-Containers {
    Write-Info "🚀 Démarrage des conteneurs avec docker-compose..."
    docker-compose up -d
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "docker-compose up échoué!"
        exit 1
    }
    
    Write-Warn "⏳ Attente que PostgreSQL soit prêt..."
    Start-Sleep -Seconds 10
    
    Write-Info "✅ Conteneurs démarrés!"
    Write-Info ""
    Write-Info "📝 URLs disponibles:"
    Write-Info "  - Swagger UI  : http://localhost:8082/swagger-ui.html"
    Write-Info "  - Backend API : http://localhost:8082"
    Write-Info "  - PostgreSQL  : localhost:5432 (sameh:sameh)"
    Write-Info ""
}

# =========================================================================
# ⬇️ DOWN
# =========================================================================
function Stop-Containers {
    Write-Info "⬇️  Arrêt des conteneurs..."
    docker-compose down
    Write-Info "✅ Conteneurs arrêtés!"
}

# =========================================================================
# 📋 LOGS
# =========================================================================
function Show-Logs {
    Write-Info "📋 Logs du backend:"
    docker-compose logs -f backend-app
}

# =========================================================================
# 🗑️ CLEAN
# =========================================================================
function Clean-All {
    Write-Warn "⚠️  Suppression complète (conteneurs + images)..."
    Read-Host "Appuyez sur Enter pour confirmer (ou Ctrl+C pour annuler)"
    
    docker-compose down --rmi all
    Write-Info "✅ Nettoyage terminé!"
}

# =========================================================================
# MAIN
# =========================================================================
switch ($Action.ToLower()) {
    "build" { Build-Project }
    "up" { 
        if (!(Test-Path "target/g2ii-0.0.1-SNAPSHOT.jar")) {
            Write-Warn "JAR non trouvé, compilation nécessaire..."
            Build-Project
        }
        Start-Containers 
    }
    "down" { Stop-Containers }
    "logs" { Show-Logs }
    "clean" { Clean-All }
    "all" {
        Build-Project
        Start-Containers
    }
    default {
        Write-Error "Action inconnue: $Action"
        Write-Host ""
        Write-Host "Utilisation: .\deploy.ps1 [build|up|down|logs|clean|all]"
        Write-Host ""
        Write-Host "Exemples:"
        Write-Host "  .\deploy.ps1 all              # Compile, build image, démarre conteneurs"
        Write-Host "  .\deploy.ps1 build            # Compile seulement"
        Write-Host "  .\deploy.ps1 up               # Démarre les conteneurs"
        Write-Host "  .\deploy.ps1 down             # Arrête les conteneurs"
        Write-Host "  .\deploy.ps1 logs             # Affiche les logs"
        Write-Host "  .\deploy.ps1 clean            # Supprime tout"
        exit 1
    }
}

Write-Info "✅ Opération réussie!"
