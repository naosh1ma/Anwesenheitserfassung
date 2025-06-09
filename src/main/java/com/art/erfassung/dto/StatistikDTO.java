package com.art.erfassung.dto;

/**
 * DTO zur Übertragung von Statistikdaten eines Studenten.
 * <p>
 * Diese Klasse dient als Data Transfer Object für die Anwesenheitsstatistik eines Studenten
 * zwischen Service-Schicht und Präsentationsschicht. Sie enthält sowohl die Studentendaten
 * als auch verschiedene statistische Werte zur Anwesenheit.
 * </p>
 */
public class StatistikDTO {

    private StudentenDTO student;
    private double gesamtAnwesenheit;
    private long entschuldigt;
    private long unentschuldigt;
    private long krank;
    private long verspaetungen;

    /**
     * Gibt die Studentendaten zurück.
     *
     * @return die Studentendaten als DTO
     */
    public StudentenDTO getStudent() {
        return student;
    }

    /**
     * Setzt die Studentendaten.
     *
     * @param student die zu setzenden Studentendaten
     */
    public void setStudent(StudentenDTO student) {
        this.student = student;
    }

    /**
     * Gibt die Gesamtanwesenheit in Prozent zurück.
     *
     * @return die Gesamtanwesenheit als Prozentwert
     */
    public double getGesamtAnwesenheit() {
        return gesamtAnwesenheit;
    }

    /**
     * Setzt die Gesamtanwesenheit in Prozent.
     *
     * @param gesamtAnwesenheit der zu setzende Prozentwert
     */
    public void setGesamtAnwesenheit(double gesamtAnwesenheit) {
        this.gesamtAnwesenheit = gesamtAnwesenheit;
    }

    /**
     * Gibt die Anzahl der entschuldigten Fehlzeiten zurück.
     *
     * @return die Anzahl der entschuldigten Fehlzeiten
     */
    public long getEntschuldigt() {
        return entschuldigt;
    }

    /**
     * Setzt die Anzahl der entschuldigten Fehlzeiten.
     *
     * @param entschuldigt die zu setzende Anzahl
     */
    public void setEntschuldigt(long entschuldigt) {
        this.entschuldigt = entschuldigt;
    }

    /**
     * Gibt die Anzahl der unentschuldigten Fehlzeiten zurück.
     *
     * @return die Anzahl der unentschuldigten Fehlzeiten
     */
    public long getUnentschuldigt() {
        return unentschuldigt;
    }

    /**
     * Setzt die Anzahl der unentschuldigten Fehlzeiten.
     *
     * @param unentschuldigt die zu setzende Anzahl
     */
    public void setUnentschuldigt(long unentschuldigt) {
        this.unentschuldigt = unentschuldigt;
    }

    /**
     * Gibt die Anzahl der Krankmeldungen zurück.
     *
     * @return die Anzahl der Krankmeldungen
     */
    public long getKrank() {
        return krank;
    }

    /**
     * Setzt die Anzahl der Krankmeldungen.
     *
     * @param krank die zu setzende Anzahl
     */
    public void setKrank(long krank) {
        this.krank = krank;
    }

    /**
     * Gibt die Anzahl der Verspätungen zurück.
     *
     * @return die Anzahl der Verspätungen
     */
    public long getVerspaetungen() {
        return verspaetungen;
    }

    /**
     * Setzt die Anzahl der Verspätungen.
     *
     * @param verspaetungen die zu setzende Anzahl
     */
    public void setVerspaetungen(long verspaetungen) {
        this.verspaetungen = verspaetungen;
    }
}