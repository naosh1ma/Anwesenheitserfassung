package com.art.erfassung.model;

import jakarta.persistence.*;

/**
 * Entität zur Darstellung einer Gruppe.
 * <p>
 * Diese Klasse repräsentiert eine Gruppe in der Datenbank, welche über einen
 * eindeutigen Identifikator und eine Bezeichnung verfügt. Sie wird mittels JPA
 * zur Persistierung der Gruppendaten verwendet.
 * </p>
 */
@Entity
@Table(name="gruppe")
public class Gruppe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "bezeichnung")
    private String bezeichnung;

    public Gruppe() {}

    public Gruppe(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public Integer getId() { return id; }
    public String getBezeichnung() { return bezeichnung; }

    public void setId(Integer id) {this.id = id;}
    public void setBezeichnung(String bezeichnung) {this.bezeichnung = bezeichnung;}
}
