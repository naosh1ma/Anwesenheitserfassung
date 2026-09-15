package com.art.erfassung.controller;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StudentenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller zur Anzeige und Aktualisierung der Anwesenheitslisten.
 * <p>
 * Alle Methoden in dieser Klasse verarbeiten Anfragen, die mit "/liste" beginnen.
 * Der Controller ermöglicht das Anzeigen der Anwesenheitsdaten einer Gruppe sowie das
 * Aktualisieren einzelner Anwesenheitseinträge.
 * </p>
 */
@Controller
@RequestMapping("/liste")
public class ListeController {

    // Service zur Verwaltung von Gruppen
    private final GruppeService gruppeService;
    // Service zur Verwaltung von Studenten
    private final StudentenService studentenService;
    // Service zur Verwaltung der Anwesenheitsdaten (Erfassungen)
    private final ErfassungService erfassungService;

    public ListeController(GruppeService gruppeService, StudentenService studentenService,
                           ErfassungService erfassungService) {
        this.gruppeService = gruppeService;
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
    }

    /**
     * Zeigt die Anwesenheitsliste für eine bestimmte Gruppe an.
     * <p>
     * Diese Methode verarbeitet GET-Anfragen an "/liste/{gruppenId}".
     * Es werden die Gruppe, ihre Studenten sowie die Anwesenheitsdaten innerhalb eines bestimmten Monats geladen.
     * Angezeigt werden alle aktiven Studenten sowie deaktivierte Studenten, die in diesem Monat Erfassungen haben.
     * Falls der Parameter "monat" nicht angegeben oder leer ist, wird der aktuelle Monat verwendet.
     * Die geladenen Daten werden dem Model hinzugefügt und an die View "anwesenheitsliste" übergeben.
     * </p>
     *
     * @param gruppenId die ID der anzuzeigenden Gruppe
     * @param monat     (optional) der Monat im Format "YYYY-MM", für den die Daten angezeigt werden sollen;
     *                  falls leer wird der aktuelle Monat verwendet
     * @param model     das Model, in dem die Daten für die View gespeichert werden
     * @return der Name der View "anwesenheitsliste"
     */
    @GetMapping("/{gruppenId}")
    public String anwesenheitAnzeigen(@PathVariable Integer gruppenId,
                                      @RequestParam(required = false) String monat,
                                      Model model) {

        // Laden der Gruppe; löst eine Exception aus, falls die Gruppe nicht existiert
        Gruppe gruppe = gruppeService.findOrThrow(gruppenId);
        // Ermitteln des Startdatums des Monats:
        // Falls der Parameter "monat" angegeben ist, wird dieser als erster Tag des Monats interpretiert.
        // Andernfalls (auch bei leerem Monatsfeld) wird der erste Tag des aktuellen Monats verwendet.
        LocalDate monatStart = (monat != null && !monat.isBlank())
                ? LocalDate.parse(monat.trim() + "-01")
                : LocalDate.now().withDayOfMonth(1);
        // Ermitteln des Enddatums des Monats
        LocalDate monatEnde = monatStart.withDayOfMonth(monatStart.lengthOfMonth());
        // Abrufen der Anwesenheitsdaten (Erfassungen) für die Gruppe innerhalb des angegebenen Zeitraums
        List<Erfassung> erfassungen = erfassungService.findByGruppeUndMonat(gruppe.getId(), monatStart, monatEnde);
        // Aktive Studenten sowie deaktivierte Studenten, die in diesem Monat noch Erfassungen haben
        Set<Integer> studentenMitErfassungen = erfassungen.stream()
                .map(erfassung -> erfassung.getStudenten().getId())
                .collect(Collectors.toSet());
        List<Studenten> studenten = studentenService.findAlleByGruppeIdSortiert(gruppe.getId()).stream()
                .filter(student -> student.isAktiv() || studentenMitErfassungen.contains(student.getId()))
                .toList();
        // Hinzufügen der geladenen Daten zum Model, damit sie in der View verfügbar sind
        model.addAttribute("gruppe", gruppe);
        model.addAttribute("studenten", studenten);
        model.addAttribute("erfassungen", erfassungen);
        // Formatierter Monat (YYYY-MM)
        model.addAttribute("monat", monatStart.toString().substring(0, 7));
        // Anzahl der Tage im Monat
        model.addAttribute("tageImMonat", monatStart.lengthOfMonth());
        // Rückgabe des View-Namens "anwesenheitsliste"
        return "anwesenheitsliste";
    }

}
