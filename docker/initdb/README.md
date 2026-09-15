# Bestehende Datenbank übernehmen (optional)

Legen Sie hier einen SQL-Dump Ihrer bisherigen Datenbank ab, um die Anwendung in Docker mit einer Kopie Ihrer Daten zu starten:

```bash
mariadb-dump -u admin_db -p anwesenheit > docker/initdb/anwesenheit.sql
```

Der MariaDB-Container liest `.sql`-, `.sql.gz`- und `.sh`-Dateien aus diesem Ordner **nur beim allerersten Start** ein, also solange das Datenbank-Volume noch leer ist. Beim anschließenden Start der Anwendung markiert Flyway die übernommene Datenbank als Version 1 und aktualisiert sie (V2 bis V6).

Wurde der Container bereits ohne Dump gestartet, entfernen Sie zuerst das Volume, damit der Dump eingelesen wird. **Achtung:** Das löscht alle Daten in der Docker-Datenbank.

```bash
docker compose down -v
docker compose up -d --build
```

Dumps enthalten personenbezogene Daten und Passwörter und werden deshalb nicht eingecheckt (siehe `.gitignore`).
