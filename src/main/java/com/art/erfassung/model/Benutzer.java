package com.art.erfassung.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entität zur Repräsentation eines Benutzers.
 * <p>
 * Diese Klasse bildet die Tabelle "benutzer" in der Datenbank ab und enthält
 * grundlegende Informationen zum Benutzer, wie Benutzernamen, Passwort-Hash, Nachname, Vorname und Rolle.
 * </p>
 */
@Entity
@Table(name = "benutzer")
public class Benutzer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "login")
    private String benutzername;

    // BCrypt-Hash des Passworts (60 Zeichen)
    @Column(name = "passwort")
    private String passwort;

    @Column(name = "name")
    private String name;

    @Column(name = "vorname")
    private String vorname;

    // Als VARCHAR gespeichert (nicht als native ENUM-Spalte), passend zur Flyway-Migration V2
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "rolle", length = 20)
    private Rolle rolle;

    protected Benutzer() {}

    public Benutzer(String benutzername, String vorname, String name, Rolle rolle) {
        this.benutzername = benutzername;
        this.vorname = vorname;
        this.name = name;
        this.rolle = rolle;
    }

    // Getter & Setter
    public Integer getId() { return id; }
    public String getBenutzername() { return benutzername; }
    public String getPasswort() { return passwort; }
    public String getName() { return name; }
    public String getVorname() { return vorname; }
    public Rolle getRolle() { return rolle; }

    public void setPasswort(String passwort) { this.passwort = passwort; }
    public void setRolle(Rolle rolle) { this.rolle = rolle; }
}
