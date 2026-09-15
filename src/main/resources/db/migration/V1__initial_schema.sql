-- Ausgangsschema, wie es vor der Einführung von Flyway bestand.
-- Bestehende Datenbanken werden beim ersten Start als Version 1 markiert (baseline);
-- dieses Skript läuft daher nur auf leeren Datenbanken.

CREATE TABLE gruppe (
    id          INT NOT NULL AUTO_INCREMENT,
    bezeichnung VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE studenten (
    id        INT NOT NULL AUTO_INCREMENT,
    name      VARCHAR(255) NOT NULL,
    vorname   VARCHAR(255) NOT NULL,
    gruppe_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_studenten_gruppe FOREIGN KEY (gruppe_id) REFERENCES gruppe (id)
);

CREATE TABLE status (
    id          INT NOT NULL AUTO_INCREMENT,
    bezeichnung VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE erfassung (
    id           INT NOT NULL AUTO_INCREMENT,
    studenten_id INT NOT NULL,
    datum        DATE NOT NULL,
    status_id    INT NOT NULL,
    kommentar    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_erfassung_studenten FOREIGN KEY (studenten_id) REFERENCES studenten (id),
    CONSTRAINT fk_erfassung_status FOREIGN KEY (status_id) REFERENCES status (id)
);

CREATE TABLE benutzer (
    id       INT NOT NULL AUTO_INCREMENT,
    login    VARCHAR(255),
    passwort VARCHAR(255),
    name     VARCHAR(255),
    vorname  VARCHAR(255),
    PRIMARY KEY (id)
);
