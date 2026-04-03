FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copie le fichier jar généré (vérifie qu'il est bien dans target/)
COPY target/*.jar app.jar

# Doit correspondre au port de ton application.yml (8082)
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]