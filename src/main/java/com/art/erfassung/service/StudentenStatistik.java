package com.art.erfassung.service;

public class StudentenStatistik {

    private double gesamtAnwesenheit;
    private long anzahlEntschuldigt;
    private long anzahlUnentschuldigt;
    private long anzahlKrank;
    private long anzahlVerspaetung;

    public StudentenStatistik(double gesamtAnwesenheit, long anzahlEntschuldigt, long anzahlUnentschuldigt, long anzahlKrank, long anzahlVerspaetung) {
        this.gesamtAnwesenheit = gesamtAnwesenheit;
        this.anzahlEntschuldigt = anzahlEntschuldigt;
        this.anzahlUnentschuldigt = anzahlUnentschuldigt;
        this.anzahlKrank = anzahlKrank;
        this.anzahlVerspaetung = anzahlVerspaetung;
    }

    // Getter und Setter

    public double getGesamtAnwesenheit() {
        return gesamtAnwesenheit;
    }

    public void setGesamtAnwesenheit(double gesamtAnwesenheit) {
        this.gesamtAnwesenheit = gesamtAnwesenheit;
    }

    public long getAnzahlEntschuldigt() {
        return anzahlEntschuldigt;
    }

    public void setAnzahlEntschuldigt(long anzahlEntschuldigt) {
        this.anzahlEntschuldigt = anzahlEntschuldigt;
    }

    public long getAnzahlUnentschuldigt() {
        return anzahlUnentschuldigt;
    }

    public void setAnzahlUnentschuldigt(long anzahlUnentschuldigt) {
        this.anzahlUnentschuldigt = anzahlUnentschuldigt;
    }

    public long getAnzahlKrank() {
        return anzahlKrank;
    }

    public void setAnzahlKrank(long anzahlKrank) {
        this.anzahlKrank = anzahlKrank;
    }

    public long getAnzahlVerspaetung() {
        return anzahlVerspaetung;
    }

    public void setAnzahlVerspaetung(long anzahlVerspaetung) {
        this.anzahlVerspaetung = anzahlVerspaetung;
    }
}

