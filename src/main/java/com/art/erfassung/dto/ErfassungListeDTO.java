package com.art.erfassung.dto;

import java.time.LocalDate;

/**
 * DTO zur Übertragung von Erfassungsdaten für die Anwesenheitsliste.
 * <p>
 * Diese Klasse dient als Data Transfer Object für Erfassungsdaten zwischen
 * Service-Schicht und Präsentationsschicht bei der Anzeige von Anwesenheitslisten.
 * Sie enthält die wesentlichen Attribute einer Erfassung, die für die Anzeige
 * in der Listenansicht benötigt werden.
 * </p>
 */
public class ErfassungListeDTO {

    private Integer id;
    private Integer studentenId;
    private String studentenName;
    private LocalDate datum;
    private Integer statusId;
    private String statusBezeichnung;
    private String kommentar;

    /**
     * Gibt die ID der Erfassung zurück.
     *
     * @return die ID der Erfassung
     */
    public Integer getId() {
        return id;
    }

    /**
     * Setzt die ID der Erfassung.
     *
     * @param id die zu setzende ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gibt die ID des Studenten zurück.
     *
     * @return die ID des Studenten
     */
    public Integer getStudentenId() {
        return studentenId;
    }

    /**
     * Setzt die ID des Studenten.
     *
     * @param studentenId die zu setzende Studenten-ID
     */
    public void setStudentenId(Integer studentenId) {
        this.studentenId = studentenId;
    }

    /**
     * Gibt den Namen des Studenten zurück.
     *
     * @return der Name des Studenten
     */
    public String getStudentenName() {
        return studentenName;
    }

    /**
     * Setzt den Namen des Studenten.
     *
     * @param studentenName der zu setzende Studentenname
     */
    public void setStudentenName(String studentenName) {
        this.studentenName = studentenName;
    }

    /**
     * Gibt das Datum der Erfassung zurück.
     *
     * @return das Datum der Erfassung
     */
    public LocalDate getDatum() {
        return datum;
    }

    /**
     * Setzt das Datum der Erfassung.
     *
     * @param datum das zu setzende Datum
     */
    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    /**
     * Gibt die ID des Status zurück.
     *
     * @return die ID des Status
     */
    public Integer getStatusId() {
        return statusId;
    }

    /**
     * Setzt die ID des Status.
     *
     * @param statusId die zu setzende Status-ID
     */
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    /**
     * Gibt die Bezeichnung des Status zurück.
     *
     * @return die Bezeichnung des Status
     */
    public String getStatusBezeichnung() {
        return statusBezeichnung;
    }

    /**
     * Setzt die Bezeichnung des Status.
     *
     * @param statusBezeichnung die zu setzende Statusbezeichnung
     */
    public void setStatusBezeichnung(String statusBezeichnung) {
        this.statusBezeichnung = statusBezeichnung;
    }

    /**
     * Gibt den Kommentar zur Erfassung zurück.
     *
     * @return der Kommentar zur Erfassung
     */
    public String getKommentar() {
        return kommentar;
    }

    /**
     * Setzt den Kommentar zur Erfassung.
     *
     * @param kommentar der zu setzende Kommentar
     */
    public void setKommentar(String kommentar) {
        this.kommentar = kommentar;
    }
}