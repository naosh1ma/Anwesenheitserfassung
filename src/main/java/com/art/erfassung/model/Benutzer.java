package com.art.erfassung.model;

import jakarta.persistence.*;

/**
 * Entität zur Repräsentation eines Benutzers.
 * <p>
 * Diese Klasse bildet die Tabelle "benutzer" in der Datenbank ab und enthält
 * grundlegende Informationen zum Benutzer, wie Benutzernamen, Passwort, Nachname und Vorname.
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

    @Column(name = "passwort")
    private String passwort;

    @Column(name = "name")
    private String name;

    @Column(name = "vorname")
    private String vorname;

    // Getter & Setter
    public String getBenutzername() { return benutzername; }
    public String getPasswort() { return passwort; }
    public String getName() { return name; }
    public String getVorname() { return vorname; }
}

