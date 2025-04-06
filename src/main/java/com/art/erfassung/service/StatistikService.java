package com.art.erfassung.service;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviceklasse zur Berechnung von Statistikdaten für Studenten.
 * <p>
 * Diese Klasse nutzt das {@link ErfassungRepository}, um Erfassungen eines Studenten abzurufen
 * und daraus verschiedene Anwesenheitsstatistiken zu ermitteln.
 */
@Service
public class StatistikService {

    // Repository zur Abfrage der Erfassungsdaten
    private final ErfassungRepository erfassungRepository;

    /**
     * Konstruktor zur Initialisierung des StatistikService mit dem erforderlichen ErfassungRepository.
     *
     * @param erfassungRepository das Repository, das zur Abfrage der Erfassungsdaten verwendet wird.
     */
    public StatistikService(ErfassungRepository erfassungRepository) {
        this.erfassungRepository = erfassungRepository;
    }

    /**
     * Berechnet die Anwesenheitsstatistik für einen Studenten anhand seiner ID.
     * <p>
     * Es werden alle Erfassungen des Studenten abgefragt und folgende Statistiken ermittelt:
     * <ul>
     *     <li>Gesamte Anwesenheitsquote (in Prozent) basierend auf dem Anteil der "Anwesend"-Einträge</li>
     *     <li>Anzahl der "Entschuldigt"-Einträge</li>
     *     <li>Anzahl der "Unentschuldigt"-Einträge</li>
     *     <li>Anzahl der "Krank"-Einträge</li>
     *     <li>Anzahl der Verspätungen (Erfassungen, deren Kommentar das Wort "verspätung" enthält)</li>
     * </ul>
     *
     * @param studentId die eindeutige ID des Studenten
     * @return ein {@link StatistikErgebnis} Objekt, das die berechneten Statistiken enthält
     */
    public StatistikErgebnis berechneFuerStudent(Integer studentId) {
        // Abrufen aller Erfassungen für den gegebenen Studenten
        List<Erfassung> erfassungen = erfassungRepository.findByStudenten_id(studentId);
        // Gesamtzahl der Erfassungen
        long total = erfassungen.size();
        // Gruppierung der Erfassungen nach Statusbezeichnung und Zählung der jeweiligen Vorkommnisse
        Map<String, Long> statusCount = erfassungen.stream()
                .collect(Collectors.groupingBy(e -> e.getStatus().getBezeichnung(), Collectors.counting()));
        // Ermittlung der Anzahl der Verspätungen:
        // Filtern der Erfassungen, deren Kommentar (falls vorhanden) das Wort "verspätung" enthält.
        long verspaetet = erfassungen.stream()
                .filter(e -> Optional.ofNullable(e.getKommentar()).orElse("").toLowerCase().contains("verspätung"))
                .count();
        // Anzahl der "Anwesend"-Einträge ermitteln, Standardwert 0 falls nicht vorhanden
        long anwesend = statusCount.getOrDefault("Anwesend", 0L);
        // Berechnung der Anwesenheitsquote in Prozent (sicherheitsüberprüfung, um Division durch Null zu vermeiden)
        double prozent = (total > 0) ? ((double) anwesend / total * 100.0) : 0.0;
        // Falls Erfassungen vorhanden sind, wird der zugehörige Student aus der ersten Erfassung übernommen, sonst null
        Studenten student = erfassungen.isEmpty() ? null : erfassungen.get(0).getStudenten();
        // Rückgabe eines neuen StatistikErgebnis-Objekts mit den berechneten Werten
        return new StatistikErgebnis(student, prozent,
                statusCount.getOrDefault("Entschuldigt", 0L),
                statusCount.getOrDefault("Unentschuldigt", 0L),
                statusCount.getOrDefault("Krank", 0L),
                verspaetet);
    }
    public record StatistikErgebnis(
            Studenten student,
            double gesamtAnwesenheit,
            long entschuldigt,
            long unentschuldigt,
            long krank,
            long verspaetungen) { }
}
