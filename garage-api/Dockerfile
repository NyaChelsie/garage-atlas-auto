# ETAPE 1 : construire le JAR avec Maven et le JDK 21.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# On copie pom.xml puis le code source, et on fabrique le JAR sans lancer les tests.
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# ETAPE 2 : image finale legere, avec seulement le JRE 21 et le JAR.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Reglages JVM pour un petit conteneur (512 Mo, 0,25 processeur) :
#   MaxRAMPercentage=75 : la JVM utilise au plus 75 % de la memoire du conteneur.
#   UseSerialGC         : ramasse-miettes simple, economique en memoire.
#   TieredStopAtLevel=1 : demarrage plus rapide sur un processeur faible.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

# Le port vient de la variable PORT fournie par l'hebergeur (8080 si elle est absente).
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
