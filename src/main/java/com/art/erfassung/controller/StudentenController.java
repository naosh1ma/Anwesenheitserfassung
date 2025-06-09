package com.art.erfassung.controller;

import com.art.erfassung.dto.StatistikDTO;
import com.art.erfassung.mapper.StatistikMapper;
import com.art.erfassung.service.StatistikService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller zur Anzeige der Anwesenheitsstatistik eines Studenten.
 * <p>
 * Dieser Controller verarbeitet GET-Anfragen an "/studenten/{id}" und ruft
 * die Anwesenheitsstatistik für den angegebenen Studenten ab. Falls keine Erfassung
 * für den Studenten vorhanden ist, erfolgt eine Weiterleitung zur Anwesenheitsseite.
 * </p>
 */
@Controller
@RequestMapping("/studenten")
public class StudentenController {

    // Service zur Berechnung der Anwesenheitsstatistik.
    private final StatistikService statistikService;

    // Mapper zur Konvertierung zwischen StatistikErgebnis und StatistikDTO.
    private final StatistikMapper statistikMapper;

    @Autowired
    public StudentenController(StatistikService statistikService, StatistikMapper statistikMapper) {
        this.statistikService = statistikService;
        this.statistikMapper = statistikMapper;
    }

    /**
     * Zeigt die Anwesenheitsstatistik eines Studenten an.
     * <p>
     * Diese Methode verarbeitet GET-Anfragen an "/studenten/{id}". Es wird die Statistik
     * für den Studenten mit der übergebenen ID berechnet. Falls keine Erfassung für den Studenten
     * vorhanden ist, erfolgt eine Weiterleitung zur Seite "/anwesenheit" als Fallback.
     * </p>
     *
     * @param studentId die ID des Studenten, dessen Statistik angezeigt werden soll
     * @param model     das Model, in das die Statistikdaten für die View eingefügt werden
     * @return den Namen der View "statistik", die die ermittelte Statistik darstellt,
     *         oder eine Weiterleitung zur Anwesenheitsseite, falls keine Erfassung vorhanden ist
     */
    @GetMapping("/{id}")
    public String getStatistik(@PathVariable("id") Integer studentId, Model model) {
        // Berechnet die Statistik für den angegebenen Studenten
        var statistikErgebnis = statistikService.berechneStudentenstatistik(studentId);
        // Falls keine Erfassung für den Studenten gefunden wurde, wird eine Weiterleitung initiiert
        if (statistikErgebnis.student() == null) {
            // Fallback, wenn keine Erfassung vorhanden ist
            return "redirect:/anwesenheit";
        }
        // Konvertiert das StatistikErgebnis in ein StatistikDTO
        StatistikDTO statistikDTO = statistikMapper.toDTO(statistikErgebnis);
        // Fügt die ermittelte Statistik dem Model hinzu, damit sie in der View verwendet werden kann
        model.addAttribute("statistik", statistikDTO);
        // Rückgabe des View-Namens "statistik"
        return "statistik";
    }
}
