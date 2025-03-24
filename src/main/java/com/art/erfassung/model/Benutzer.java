package com.art.erfassung.model;

import jakarta.persistence.*;

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

