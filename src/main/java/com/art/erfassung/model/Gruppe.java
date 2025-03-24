package com.art.erfassung.model;

import jakarta.persistence.*;

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

    public void setBezeichnung(String bezeichnung) {this.bezeichnung = bezeichnung;}
}
