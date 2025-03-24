package com.art.erfassung.model;

import jakarta.persistence.*;

@Entity
@Table(name="status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "bezeichnung")
    private String bezeichnung;

    public Status() {}

    public Status(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public int getId() {return id;}
    public String getBezeichnung() {return bezeichnung;}

    public void setBezeichnung(String bezeichnung) {this.bezeichnung = bezeichnung;}
}
