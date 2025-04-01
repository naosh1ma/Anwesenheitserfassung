package com.art.erfassung.controller;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.service.StudentenStatistik;
import com.art.erfassung.repository.ErfassungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/studenten")
public class StudentenController {

    @Autowired
    private ErfassungRepository erfassungRepository;

    public StudentenController(ErfassungRepository erfassungRepository) {
        this.erfassungRepository = erfassungRepository;
    }

    @GetMapping("/{id}")
    public String getStudentenStatistik(@PathVariable("id") Integer studentId, Model model) {
        // Holen der Erfassungen des Studenten
        List<Erfassung> erfassungen = erfassungRepository.findByStudenten_id(studentId);

        // Berechnung der Statistiken
        double gesamtAnwesenheit = berechneGesamtAnwesenheit(erfassungen);
        long anzahlEntschuldigt = erfassungen.stream().filter(e -> e.getStatus().getBezeichnung().equals("Entschuldigt")).count();
        long anzahlUnentschuldigt = erfassungen.stream().filter(e -> e.getStatus().getBezeichnung().equals("Unentschuldigt")).count();
        long anzahlKrank = erfassungen.stream().filter(e -> e.getStatus().getBezeichnung().equals("Krank")).count();
        long anzahlVerspaetung = erfassungen.stream().filter(e -> e.getKommentar() != null && e.getKommentar().toLowerCase().contains("verspätung")).count();

        // Berechnen der Gesamtanwesenheit in Prozent
        long anzahlAnwesend = erfassungen.stream().filter(e -> e.getStatus().getBezeichnung().equals("Anwesend")).count();
        long totalAnwesenheit = erfassungen.size();
        gesamtAnwesenheit = (totalAnwesenheit > 0) ? ((double) anzahlAnwesend / totalAnwesenheit) * 100 : 0;

        // Modell für die Anzeige
        model.addAttribute("statistik", new StudentenStatistik(gesamtAnwesenheit, anzahlEntschuldigt, anzahlUnentschuldigt, anzahlKrank, anzahlVerspaetung));
        model.addAttribute("student", erfassungen.get(0).getStudenten());

        return "statistik"; // Die HTML-Seite, die du vorher erstellt hast
    }

    private double berechneGesamtAnwesenheit(List<Erfassung> erfassungen) {
        long anzahlAnwesend = erfassungen.stream().filter(e -> e.getStatus().getBezeichnung().equals("Anwesend")).count();
        long totalAnwesenheit = erfassungen.size();
        return (totalAnwesenheit > 0) ? ((double) anzahlAnwesend / totalAnwesenheit) * 100 : 0;
    }
}

