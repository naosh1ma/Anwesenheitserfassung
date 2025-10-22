# Erfassung - Anwesenheitserfassung System

Ein Spring Boot Web-Anwendung zur Verwaltung von Studentenanwesenheit in Bildungseinrichtungen.

## Features

- **Gruppenverwaltung**: Erstellen und verwalten von Studentengruppen
- **Studentenverwaltung**: Speichern von Studenteninformationen
- **Anwesenheitserfassung**: Aufzeichnung von Anwesenheitsstatus mit Kommentaren
- **Statistiken**: Anzeige von Anwesenheitsstatistiken und Berichten
- **Sicherheit**: Authentifizierung und rollenbasierte Autorisierung

## Technologie-Stack

- **Backend**: Spring Boot 3.4.3, Java 23
- **Datenbank**: MariaDB (Produktion), H2 (Tests)
- **Frontend**: Thymeleaf Templates, CSS
- **Sicherheit**: Spring Security
- **Build Tool**: Maven

## Voraussetzungen

- Java 23 oder höher
- Maven 3.6+
- MariaDB (für Produktion)
- Git

## Installation und Setup

### 1. Repository klonen

```bash
git clone <repository-url>
cd erfassung
```

### 2. Datenbank einrichten

#### MariaDB Setup (Produktion)
```sql
CREATE DATABASE anwesenheit;
CREATE USER 'admin_db'@'localhost' IDENTIFIED BY 'M6950evajo';
GRANT ALL PRIVILEGES ON anwesenheit.* TO 'admin_db'@'localhost';
FLUSH PRIVILEGES;
```

#### H2 Setup (Tests)
H2 wird automatisch für Tests verwendet - keine manuelle Einrichtung erforderlich.

### 3. Anwendung konfigurieren

#### Entwicklungsumgebung
Die Anwendung verwendet standardmäßig das `dev` Profil. Die Konfiguration befindet sich in `src/main/resources/application-dev.properties`.

#### Produktionsumgebung
Für die Produktion setzen Sie die folgenden Umgebungsvariablen:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mariadb://localhost:3306/anwesenheit
export DB_USERNAME=admin_db
export DB_PASSWORD=your_secure_password
```

### 4. Anwendung starten

```bash
# Mit Maven
./mvnw spring-boot:run

# Oder mit Java
./mvnw clean package
java -jar target/erfassung-0.0.1-SNAPSHOT.jar
```

Die Anwendung ist dann unter `http://localhost:8080` erreichbar.

## Standard-Zugangsdaten

### Administrator
- **Benutzername**: admin
- **Passwort**: admin123
- **Berechtigungen**: Vollzugriff auf alle Funktionen

### Lehrer
- **Benutzername**: teacher
- **Passwort**: teacher123
- **Berechtigungen**: Anwesenheitserfassung und Statistiken

## Projektstruktur

```
src/
├── main/
│   ├── java/com/art/erfassung/
│   │   ├── config/          # Konfigurationsklassen
│   │   ├── controller/      # Web-Controller
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── error/          # Fehlerbehandlung
│   │   ├── mapper/         # Entity-DTO Mapper
│   │   ├── model/          # JPA-Entitäten
│   │   ├── repository/     # Datenzugriffsschicht
│   │   └── service/        # Geschäftslogik
│   └── resources/
│       ├── static/         # Statische Ressourcen (CSS, JS, Bilder)
│       └── templates/      # Thymeleaf-Templates
└── test/                   # Testklassen
```

## Entwicklung

### Tests ausführen
```bash
./mvnw test
```

### Code-Qualität prüfen
```bash
./mvnw clean compile
```

### Anwendung mit Tests starten
```bash
./mvnw spring-boot:run -Dspring.profiles.active=test
```

## Sicherheit

### Wichtige Sicherheitshinweise

1. **Passwörter ändern**: Ändern Sie die Standard-Passwörter vor der Produktionsnutzung
2. **Datenbank-Zugangsdaten**: Verwenden Sie starke Passwörter und sichere Verbindungen
3. **HTTPS**: Aktivieren Sie HTTPS in der Produktionsumgebung
4. **CSRF-Schutz**: CSRF-Schutz ist derzeit für die Entwicklung deaktiviert

### Umgebungsvariablen für Produktion

```bash
# Datenbank
DB_URL=jdbc:mariadb://your-db-host:3306/anwesenheit
DB_USERNAME=your-db-user
DB_PASSWORD=your-secure-password

# Spring Profil
SPRING_PROFILES_ACTIVE=prod
```

## API-Endpunkte

### Öffentliche Endpunkte
- `GET /` - Login-Seite
- `POST /login` - Authentifizierung
- `GET /logout` - Abmeldung

### Geschützte Endpunkte
- `GET /willkommen` - Willkommensseite
- `GET /gruppen` - Gruppenübersicht
- `GET /anwesenheit/{gruppeId}` - Anwesenheitserfassung
- `POST /anwesenheit/speichern` - Anwesenheit speichern
- `GET /statistik` - Statistiken anzeigen

## Beitragen

1. Fork das Repository
2. Erstellen Sie einen Feature-Branch (`git checkout -b feature/AmazingFeature`)
3. Committen Sie Ihre Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Pushen Sie zum Branch (`git push origin feature/AmazingFeature`)
5. Öffnen Sie einen Pull Request

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert.

## Support

Bei Fragen oder Problemen erstellen Sie bitte ein Issue im Repository.
