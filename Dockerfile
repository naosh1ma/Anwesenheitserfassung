# Baut die Anwendung und erzeugt ein schlankes Laufzeit-Image.
# Die Tests laufen in der CI (GitHub Actions), nicht beim Image-Build.
# Image-Namen sind voll qualifiziert (docker.io/...), damit sie auch mit Podman ohne Rückfrage aufgelöst werden.

# ---- Build: kompiliert und paketiert die Anwendung mit Maven ----
FROM docker.io/library/eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
# Der Cache-Mount behält heruntergeladene Maven-Abhängigkeiten zwischen Builds
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests

# ---- Laufzeit: nur das JRE und das fertige JAR, ohne Root-Rechte ----
FROM docker.io/library/eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system erfassung && useradd --system --gid erfassung --no-create-home erfassung
COPY --from=build /workspace/target/erfassung-0.0.1-SNAPSHOT.jar app.jar
USER erfassung
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
