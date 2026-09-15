# Erfassung - Anwesenheitserfassung System

Ein Spring Boot Web-Anwendung zur Verwaltung von Studentenanwesenheit in Bildungseinrichtungen.

## Features

- **Gruppenverwaltung**: Erstellen und verwalten von Studentengruppen
- **Studentenverwaltung**: Speichern von Studenteninformationen
- **Anwesenheitserfassung**: Aufzeichnung von Anwesenheitsstatus mit Kommentaren
- **Statistiken**: Anzeige von Anwesenheitsstatistiken und Berichten
- **Sicherheit**: Authentifizierung und rollenbasierte Autorisierung

## Technologie-Stack

- **Backend**: Spring Boot 4.1, Java 25
- **Datenbank**: MariaDB (Produktion), H2 (Tests), Schema-Verwaltung mit Flyway
- **Frontend**: Thymeleaf Templates, CSS
- **Sicherheit**: Spring Security
- **Build Tool**: Maven

## Voraussetzungen

- Java 25 oder höher
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
CREATE USER 'admin_db'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON anwesenheit.* TO 'admin_db'@'localhost';
FLUSH PRIVILEGES;
```

#### H2 Setup (Tests)
H2 wird automatisch für Tests verwendet - keine manuelle Einrichtung erforderlich.

### 3. Anwendung konfigurieren

#### Entwicklungsumgebung
Für die Entwicklung aktivieren Sie das `dev` Profil. Standardmäßig ist kein Profil aktiv, ohne Profil startet die Anwendung nicht. Die Konfiguration befindet sich in `src/main/resources/application-dev.properties`.
Das Datenbank-Passwort wird nicht im Repository gespeichert, sondern über Umgebungsvariablen gesetzt
(z. B. in der IntelliJ-Run-Configuration):

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_PASSWORD=your_secure_password
# Optional, falls abweichend von den Standardwerten
export DB_URL=jdbc:mariadb://localhost:3306/anwesenheit
export DB_USERNAME=admin_db
```

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
# Mit Maven (Entwicklung)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Oder mit Java (Produktion)
./mvnw clean package
java -jar target/erfassung-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Die Anwendung ist dann unter `http://localhost:8080` erreichbar.

## Benutzer und Anmeldung

Es gibt keine fest eingebauten Zugangsdaten. Benutzer werden in der Tabelle `benutzer` gespeichert, Passwörter nur als BCrypt-Hash.

### Erster Administrator
Setzen Sie vor dem ersten Start (Entwicklung und Produktion):

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=ein-sicheres-passwort   # mindestens 8 Zeichen
```

Existiert beim Start noch kein Administrator, wird dieser Benutzer angelegt (ein vorhandener Benutzer mit diesem Namen wird zum Administrator gemacht und erhält das Passwort). Sobald ein Administrator existiert, werden die Variablen ignoriert.

### Weitere Benutzer
Administratoren verwalten Benutzer über **Benutzer** im Menü (`/admin/benutzer`): anlegen, Passwort neu setzen und löschen. Das eigene Konto kann nicht gelöscht werden.

### Rollen
- **Administrator**: Benutzerverwaltung, Anwesenheitserfassung und Statistiken
- **Lehrer**: Anwesenheitserfassung und Statistiken

### Bestehende Benutzer aus früheren Versionen
Frühere Versionen haben Passwörter im Klartext gespeichert. Beim Start werden solche Passwörter automatisch gehasht, Benutzer ohne Rolle werden Lehrer. Die Anmeldedaten bleiben gleich.

Die Spalte `rolle` und ausreichend Platz für die Passwort-Hashes werden beim Start automatisch durch die Datenbank-Migrationen angelegt (siehe „Datenbank-Migrationen“).

## Anwesenheitserfassung

### Ankunfts- und Verlassen-Zeit
Ankunfts- und Verlassen-Zeit werden pro Erfassung in eigenen Spalten gespeichert (`ankunftszeit`, `verlassen_um`). Wird das Formular am selben Tag erneut geöffnet, sind die bereits gespeicherten Werte vorausgefüllt. Beim Speichern wird geprüft, dass alle Studenten zur Gruppe gehören und die Verlassen-Zeit nicht vor der Ankunftszeit liegt; ist ein Eintrag ungültig, wird nichts gespeichert.

### Verspätungen
Eine Ankunft nach dem Unterrichtsbeginn zählt als Verspätung. Der Unterrichtsbeginn ist standardmäßig 08:00 und kann über eine Umgebungsvariable geändert werden:

```bash
export UNTERRICHTSBEGINN=08:30
```

Ältere Erfassungen ohne gespeicherte Ankunftszeit zählen weiterhin als Verspätung, wenn ihr Kommentar „Verspätung“ enthält, da Verspätungen früher nur im Kommentar vermerkt wurden.

Die Spalten werden beim Start automatisch durch die Datenbank-Migrationen angelegt (siehe unten).

## Datenbank-Migrationen (Flyway)

Das Datenbankschema wird mit Flyway verwaltet. Die Migrationen liegen in `src/main/resources/db/migration` und werden beim Start der Anwendung in jedem Profil automatisch ausgeführt. Hibernate prüft nur noch, ob die Entitäten zum Schema passen (`ddl-auto=validate`). Die Tests führen dieselben Migrationen auf H2 im MariaDB-Kompatibilitätsmodus aus.

| Version | Inhalt |
|---|---|
| V1 | Ausgangsschema (Tabellen `gruppe`, `studenten`, `status`, `erfassung`, `benutzer`) |
| V2 | Spalte `benutzer.rolle` (als `VARCHAR`), `benutzer.passwort` mit 255 Zeichen |
| V3 | Spalten `erfassung.ankunftszeit` und `erfassung.verlassen_um`, Kommentare bis 500 Zeichen |
| V4 | Status-Werte „Anwesend“, „Entschuldigt“, „Unentschuldigt“ und „Krankmeldung“ |
| V5 | Spalte `studenten.deaktiviert_am` (leer bedeutet aktiv) |
| V6 | Höchstens eine Erfassung pro Student und Tag (Unique-Constraint `uk_erfassung_student_datum`) |

**Bestehende Datenbanken**: Beim ersten Start mit Flyway wird eine bereits vorhandene Datenbank als Version 1 markiert, danach laufen V2 bis V6. Die Migrationen V2 bis V5 prüfen selbst, ob Spalten und Status-Werte schon vorhanden sind. Sie funktionieren daher für ältere Datenbanken ebenso wie für Datenbanken, in denen frühere Versionen die Spalten bereits angelegt haben. Legen Sie vor dem ersten Start trotzdem eine Sicherung an.

**Schema ändern**: Änderungen immer als neue Migration anlegen (z. B. `V7__beschreibung.sql`). Bereits ausgeführte Migrationen dürfen nicht nachträglich geändert werden.

### Doppelte Erfassungen (V6)

Ab V6 erlaubt die Datenbank höchstens eine Erfassung pro Student und Tag. Enthält eine bestehende Datenbank bereits doppelte Erfassungen, bricht die Migration ab und die Anwendung startet nicht. Es wird nichts automatisch gelöscht. Die Fehlermeldung von MariaDB nennt den ersten doppelten Eintrag, z. B. `Duplicate entry '12-2026-09-01' for key 'uk_erfassung_student_datum'` (Studenten-ID und Datum).

So beheben Sie das:

1. Doppelte Erfassungen anzeigen:
   ```sql
   SELECT e.*
   FROM erfassung e
   JOIN (SELECT studenten_id, datum FROM erfassung GROUP BY studenten_id, datum HAVING COUNT(*) > 1) d
     ON e.studenten_id = d.studenten_id AND e.datum = d.datum
   ORDER BY e.studenten_id, e.datum, e.id;
   ```
2. Pro Student und Tag entscheiden, welche Erfassung gilt, und die übrigen löschen, z. B. `DELETE FROM erfassung WHERE id = 123;`
3. Den fehlgeschlagenen Migrationsversuch entfernen. MariaDB kann Schemaänderungen nicht zurückrollen, deshalb merkt sich Flyway den Fehlschlag und würde sonst nicht erneut starten:
   ```sql
   DELETE FROM flyway_schema_history WHERE success = 0;
   ```
4. Die Anwendung neu starten. V6 wird dann angewendet.

## Gruppen und Studenten verwalten

Administratoren verwalten Gruppen und Studenten über **Gruppen verwalten** im Menü (`/admin/gruppen`):

- **Gruppen**: anlegen, umbenennen und löschen. Eine Gruppe kann nur gelöscht werden, wenn sie keine Studenten mehr enthält, auch keine deaktivierten.
- **Studenten**: hinzufügen, Vor- und Nachnamen ändern und in eine andere Gruppe verschieben. Bereits erfasste Anwesenheitsdaten gehören weiterhin zum Studenten und erscheinen danach in der neuen Gruppe.
- **Deaktivieren**: Studenten, die eine Gruppe verlassen, werden deaktiviert. Sie erscheinen nicht mehr im Erfassungsformular, ihre bisherigen Anwesenheitsdaten bleiben in der Monatsübersicht (dort als „deaktiviert“ markiert) und in der Statistik erhalten. Deaktivierte Studenten können jederzeit reaktiviert werden.
- **Löschen**: Nur Studenten ohne Anwesenheitsdaten können gelöscht werden, damit keine erfassten Daten verloren gehen.

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

### Automatische Tests (GitHub Actions)
Bei jedem Push und Pull Request auf `master` baut GitHub Actions das Projekt und führt alle Tests aus (`.github/workflows/ci.yml`). Die Tests verwenden die H2-In-Memory-Datenbank, eine MariaDB ist dafür nicht nötig.

## Sicherheit

### Wichtige Sicherheitshinweise

1. **Administrator-Passwort**: Verwenden Sie für `ADMIN_PASSWORD` ein starkes Passwort. Nach dem ersten Start wird die Variable nicht mehr benötigt und kann entfernt werden
2. **Datenbank-Zugangsdaten**: Verwenden Sie starke Passwörter und sichere Verbindungen
3. **HTTPS**: Aktivieren Sie HTTPS in der Produktionsumgebung
4. **CSRF-Schutz**: CSRF-Schutz ist aktiviert. POST-Formulare müssen mit `th:action` gerendert werden, damit das Token eingefügt wird (auch das Abmelden erfolgt per POST)

### Umgebungsvariablen für Produktion

```bash
# Datenbank
DB_URL=jdbc:mariadb://your-db-host:3306/anwesenheit
DB_USERNAME=your-db-user
DB_PASSWORD=your-secure-password

# Erster Administrator (nur nötig, solange noch kein Administrator existiert)
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-secure-admin-password

# Unterrichtsbeginn für die Verspätungsberechnung (optional, Standard 08:00)
UNTERRICHTSBEGINN=08:00

# Spring Profil
SPRING_PROFILES_ACTIVE=prod
```

## API-Endpunkte

### Öffentliche Endpunkte
- `GET /`, `GET /login` - Login-Seite
- `POST /login` - Authentifizierung
- `POST /logout` - Abmeldung

### Geschützte Endpunkte
- `GET /willkommen` - Willkommensseite
- `GET /gruppen` - Gruppenübersicht
- `GET /anwesenheit/{gruppeId}` - Anwesenheitserfassung
- `POST /anwesenheit/{gruppeId}/speichern` - Anwesenheit speichern
- `GET /liste/{gruppeId}` - Monatliche Anwesenheitsliste einer Gruppe
- `GET /studenten/{studentId}` - Statistik eines Studenten

### Nur für Administratoren
- `GET /admin/benutzer` - Benutzerverwaltung
- `POST /admin/benutzer` - Benutzer anlegen
- `POST /admin/benutzer/{id}/passwort` - Passwort neu setzen
- `POST /admin/benutzer/{id}/loeschen` - Benutzer löschen
- `GET /admin/gruppen` - Gruppenverwaltung
- `POST /admin/gruppen` - Gruppe anlegen
- `POST /admin/gruppen/{id}/umbenennen` - Gruppe umbenennen
- `POST /admin/gruppen/{id}/loeschen` - Leere Gruppe löschen
- `GET /admin/gruppen/{id}` - Studenten einer Gruppe verwalten
- `POST /admin/gruppen/{id}/studenten` - Student hinzufügen
- `POST /admin/gruppen/{id}/studenten/{studentId}` - Student bearbeiten oder in eine andere Gruppe verschieben
- `POST /admin/gruppen/{id}/studenten/{studentId}/deaktivieren` - Student deaktivieren
- `POST /admin/gruppen/{id}/studenten/{studentId}/reaktivieren` - Student reaktivieren
- `POST /admin/gruppen/{id}/studenten/{studentId}/loeschen` - Student ohne Anwesenheitsdaten löschen

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
