package com.art.erfassung.controller;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StudentenService;
import com.art.erfassung.service.ZugriffService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller zur Anzeige der Anwesenheitslisten.
 * <p>
 * Alle Methoden in dieser Klasse verarbeiten Anfragen, die mit "/liste" beginnen.
 * Der Controller zeigt die Anwesenheitsdaten einer Gruppe für einen Monat an.
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

    // Service zur Prüfung, ob der Benutzer die Gruppe sehen darf
    private final ZugriffService zugriffService;

    public ListeController(GruppeService gruppeService, StudentenService studentenService,
                           ErfassungService erfassungService, ZugriffService zugriffService) {
        this.gruppeService = gruppeService;
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
        this.zugriffService = zugriffService;
    }

    /**
     * Zeigt die Anwesenheitsliste für eine bestimmte Gruppe an.
     * <p>
     * Diese Methode verarbeitet GET-Anfragen an "/liste/{gruppenId}".
     * Es werden die Gruppe, ihre Studenten sowie die Anwesenheitsdaten innerhalb eines bestimmten Monats geladen.
     * Angezeigt werden alle aktiven Studenten sowie deaktivierte Studenten, die in diesem Monat Erfassungen haben.
     * Falls der Parameter "monat" nicht angegeben oder leer ist, wird der aktuelle Monat verwendet.
     * </p>
     *
     * @param gruppenId die ID der anzuzeigenden Gruppe
     * @param monat          (optional) der Monat im Format "YYYY-MM"; falls leer wird der aktuelle Monat verwendet
     * @param authentication der angemeldete Benutzer
     * @param model          das Model, in dem die Daten für die View gespeichert werden
     * @return der Name der View "anwesenheitsliste"
     */
    @GetMapping("/{gruppenId}")
    public String anwesenheitAnzeigen(@PathVariable Integer gruppenId,
                                      @RequestParam(required = false) String monat,
                                      Authentication authentication, Model model) {

        // Lehrer sehen nur die ihnen zugewiesenen Gruppen
        zugriffService.pruefeGruppe(gruppenId, authentication);
        // Laden der Gruppe; löst eine Exception aus, falls die Gruppe nicht existiert
        Gruppe gruppe = gruppeService.findOrThrow(gruppenId);
        // Ermitteln des Monats: angegebener Monat oder (auch bei leerem Monatsfeld) der aktuelle Monat
        YearMonth anzeigeMonat = (monat != null && !monat.isBlank())
                ? YearMonth.parse(monat.trim())
                : YearMonth.now();
        LocalDate monatStart = anzeigeMonat.atDay(1);
        LocalDate monatEnde = anzeigeMonat.atEndOfMonth();
        // Abrufen der Anwesenheitsdaten (Erfassungen) für die Gruppe innerhalb des Monats
        List<Erfassung> erfassungen = erfassungService.findByGruppeUndMonat(gruppe.getId(), monatStart, monatEnde);
        // Aktive Studenten sowie deaktivierte Studenten, die in diesem Monat noch Erfassungen haben
        Set<Integer> studentenMitErfassungen = erfassungen.stream()
                .map(erfassung -> erfassung.getStudenten().getId())
                .collect(Collectors.toSet());
        List<Studenten> studenten = studentenService.findAlleByGruppeIdSortiert(gruppe.getId()).stream()
                .filter(student -> student.isAktiv() || studentenMitErfassungen.contains(student.getId()))
                .toList();
        // Erfassungen je Student und Tag des Monats, damit das Template jede Zelle direkt nachschlagen kann
        Map<Integer, Map<Integer, Erfassung>> zellen = new HashMap<>();
        for (Studenten student : studenten) {
            zellen.put(student.getId(), new HashMap<>());
        }
        for (Erfassung erfassung : erfassungen) {
            Map<Integer, Erfassung> tageDesStudenten = zellen.get(erfassung.getStudenten().getId());
            if (tageDesStudenten != null) {
                tageDesStudenten.putIfAbsent(erfassung.getDatum().getDayOfMonth(), erfassung);
            }
        }
        // Hinzufügen der geladenen Daten zum Model, damit sie in der View verfügbar sind
        model.addAttribute("gruppe", gruppe);
        model.addAttribute("studenten", studenten);
        model.addAttribute("zellen", zellen);
        model.addAttribute("tage", monatStart.datesUntil(monatEnde.plusDays(1)).toList());
        model.addAttribute("heute", LocalDate.now());
        model.addAttribute("monatStart", monatStart);
        // Formatierter Monat (YYYY-MM) sowie Vor- und Folgemonat für die Monatsnavigation
        model.addAttribute("monat", anzeigeMonat.toString());
        model.addAttribute("vorherigerMonat", anzeigeMonat.minusMonths(1).toString());
        model.addAttribute("naechsterMonat", anzeigeMonat.plusMonths(1).toString());
        // Rückgabe des View-Namens "anwesenheitsliste"
        return "anwesenheitsliste";
    }

}
