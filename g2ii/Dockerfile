#FROM eclipse-temurin:17-jdk-alpine
#WORKDIR /app
#
## Copie le fichier jar généré (vérifie qu'il est bien dans target/)
#COPY target/*.jar app.jar
#
## Doit correspondre au port de ton application.yml (8082)
#EXPOSE 8082
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

# ==========================================
# Étape 1 : Compilation de l'application (JDK)
# ==========================================
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Cache des dépendances Maven pour accélérer les builds futurs en CI/CD
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie du code source et compilation du fichier JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Étape 2 : Image d'exécution finale (JRE léger)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Création d'un utilisateur système non-root sécurisé
RUN addgroup -S sagemgroup && adduser -S sagemuser -G sagemgroup
USER sagemuser

# Récupération du JAR généré à la première étape (Phase 'build')
COPY --from=build /app/target/*.jar app.jar

# Doit correspondre au port de ton application.yml
EXPOSE 8082

# Variables d'environnement modifiables au déploiement (Docker Compose / K8s)
ENV PORT=8082
ENV SPRING_PROFILES_ACTIVE=prod

# Entropie optimisée (SecureRandom) pour accélérer le démarrage de Spring Boot sous Linux
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]