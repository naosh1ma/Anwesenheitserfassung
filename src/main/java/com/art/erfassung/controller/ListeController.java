package com.art.erfassung.controller;

import com.art.erfassung.dto.ErfassungListeDTO;
import com.art.erfassung.dto.GruppeDTO;
import com.art.erfassung.dto.StudentenDTO;
import com.art.erfassung.mapper.ErfassungListeMapper;
import com.art.erfassung.mapper.GruppeMapper;
import com.art.erfassung.mapper.StudentenMapper;
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
     * Falls der Parameter "monat" nicht angegeben wird, wird der aktuelle Monat verwendet.
     * Die geladenen Daten werden dem Model hinzugefügt und an die View "anwesenheitsliste" übergeben.
     * </p>
     *
     * @param gruppenId die ID der anzuzeigenden Gruppe
     * @param monat     (optional) der Monat im Format "YYYY-MM", für den die Daten angezeigt werden sollen;
     *                  falls null wird der aktuelle Monat verwendet
     * @param model     das Model, in dem die Daten für die View gespeichert werden
     * @return der Name der View "anwesenheitsliste"
     */
    @GetMapping("/{gruppenId}")
    public String anwesenheitAnzeigen(@PathVariable Integer gruppenId,
                                      @RequestParam(required = false) String monat,
                                      Model model) {

        // Laden der Gruppe; löst eine Exception aus, falls die Gruppe nicht existiert
        Gruppe gruppe = gruppeService.findOrThrow(gruppenId);
        // Abrufen der Studenten, die der Gruppe zugeordnet sind
        List<Studenten> studenten = studentenService.findByGruppeId(gruppe.getId());
        // Ermitteln des Startdatums des Monats:
        // Falls der Parameter "monat" angegeben ist, wird dieser als erster Tag des Monats interpretiert.
        // Andernfalls wird der erste Tag des aktuellen Monats verwendet.
        LocalDate monatStart = (monat != null) ? LocalDate.parse(monat + "-01") : LocalDate.now().withDayOfMonth(1);
        // Ermitteln des Enddatums des Monats
        LocalDate monatEnde = monatStart.withDayOfMonth(monatStart.lengthOfMonth());
        // Abrufen der Anwesenheitsdaten (Erfassungen) für die Gruppe innerhalb des angegebenen Zeitraums
        List<Erfassung> erfassungen = erfassungService.findByGruppeUndMonat(gruppe.getId(), monatStart, monatEnde);
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
