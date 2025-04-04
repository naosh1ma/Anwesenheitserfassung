package com.art.erfassung.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entität zur Darstellung einer Erfassung.
 * <p>
 * Diese Klasse bildet die Tabelle "erfassung" in der Datenbank ab. Sie speichert Informationen
 * zu einer Erfassung, die einem Studenten an einem bestimmten Datum zugeordnet ist und einen
 * bestimmten Status sowie einen optionalen Kommentar beinhaltet.
 * </p>
 */
@Entity
@Table(name = "erfassung")
public class Erfassung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studenten_id", nullable = false)
    private Studenten studenten;

    @Column(name = "datum",nullable = false)
    private LocalDate datum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Column(name = "kommentar")
    private String kommentar;

    public Erfassung() {}

    public Erfassung(Studenten student, LocalDate datum, Status status, String kommentar) {
        this.studenten = student;
        this.datum = datum;
        this.status = status;
        this.kommentar = kommentar;
    }

    public Integer getId() {return id;}
    public Studenten getStudenten() {return studenten;}
    public Status getStatus() {return status;}
    public LocalDate getDatum() {return datum;}
    public String getKommentar() {return kommentar;}

    public void setStudenten(Studenten studenten) {this.studenten = studenten;}
    public void setDatum(LocalDate datum) {this.datum = datum;}
    public void setStatus(Status status) {this.status = status;}
    public void setKommentar(String kommentar) {this.kommentar = kommentar;}

}
