package com.art.erfassung.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entität zur Darstellung eines Studenten.
 * <p>
 * Diese Klasse bildet die Tabelle "studenten" in der Datenbank ab und speichert
 * grundlegende Informationen über einen Studenten, wie Name, Vorname, die zugehörige Gruppe
 * und ob der Student noch aktiv ist.
 * </p>
 */
@Entity
@Table(name="studenten")
public class Studenten {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name ="vorname", nullable = false)
    private String vorname;

    @ManyToOne
    @JoinColumn(name = "gruppe_id", nullable = false)
    private Gruppe gruppe;

    // Datum der Deaktivierung; leer bedeutet, dass der Student aktiv ist (Flyway-Migration V5)
    @Column(name = "deaktiviert_am")
    private LocalDate deaktiviertAm;

    public Studenten() {}

    public Studenten(String name, String vorname, Gruppe gruppe) {
        this.name = name;
        this.vorname = vorname;
        this.gruppe = gruppe;
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public String getVorname() {return vorname;}
    public Gruppe getGruppe() {return gruppe;}
    public LocalDate getDeaktiviertAm() {return deaktiviertAm;}
    public boolean isAktiv() {return deaktiviertAm == null;}

    public void setId(int id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setVorname(String vorname) {this.vorname = vorname;}
    public void setGruppe(Gruppe gruppeId) {this.gruppe = gruppeId;}
    public void setDeaktiviertAm(LocalDate deaktiviertAm) {this.deaktiviertAm = deaktiviertAm;}


}
