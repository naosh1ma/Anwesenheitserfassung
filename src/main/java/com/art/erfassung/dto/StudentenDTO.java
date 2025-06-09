package com.art.erfassung.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO zur Übertragung von Studentendaten.
 * <p>
 * Diese Klasse dient als Data Transfer Object für Studentendaten zwischen
 * Service-Schicht und Präsentationsschicht. Sie enthält die wesentlichen
 * Attribute eines Studenten, die für die Anzeige oder Bearbeitung benötigt werden.
 * </p>
 */
public class StudentenDTO {

    private Integer id;

    @NotNull(message = "Name darf nicht null sein")
    @Size(min = 1, max = 100, message = "Name muss zwischen 1 und 100 Zeichen lang sein")
    private String name;

    @NotNull(message = "Vorname darf nicht null sein")
    @Size(min = 1, max = 100, message = "Vorname muss zwischen 1 und 100 Zeichen lang sein")
    private String vorname;

    private Integer gruppeId;
    private String gruppeBezeichnung;

    /**
     * Gibt die ID des Studenten zurück.
     *
     * @return die ID des Studenten
     */
    public Integer getId() {
        return id;
    }

    /**
     * Setzt die ID des Studenten.
     *
     * @param id die zu setzende ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gibt den Nachnamen des Studenten zurück.
     *
     * @return der Nachname des Studenten
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Nachnamen des Studenten.
     *
     * @param name der zu setzende Nachname
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gibt den Vornamen des Studenten zurück.
     *
     * @return der Vorname des Studenten
     */
    public String getVorname() {
        return vorname;
    }

    /**
     * Setzt den Vornamen des Studenten.
     *
     * @param vorname der zu setzende Vorname
     */
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    /**
     * Gibt die ID der Gruppe zurück, zu der der Student gehört.
     *
     * @return die ID der Gruppe
     */
    public Integer getGruppeId() {
        return gruppeId;
    }

    /**
     * Setzt die ID der Gruppe, zu der der Student gehört.
     *
     * @param gruppeId die zu setzende Gruppen-ID
     */
    public void setGruppeId(Integer gruppeId) {
        this.gruppeId = gruppeId;
    }

    /**
     * Gibt die Bezeichnung der Gruppe zurück, zu der der Student gehört.
     *
     * @return die Bezeichnung der Gruppe
     */
    public String getGruppeBezeichnung() {
        return gruppeBezeichnung;
    }

    /**
     * Setzt die Bezeichnung der Gruppe, zu der der Student gehört.
     *
     * @param gruppeBezeichnung die zu setzende Gruppenbezeichnung
     */
    public void setGruppeBezeichnung(String gruppeBezeichnung) {
        this.gruppeBezeichnung = gruppeBezeichnung;
    }
}