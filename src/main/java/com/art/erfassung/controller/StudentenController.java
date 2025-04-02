package com.art.erfassung.controller;

import com.art.erfassung.service.StatistikService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/studenten")
public class StudentenController {

    private final StatistikService statistikService;

    public StudentenController(StatistikService statistikService) {
        this.statistikService = statistikService;
    }

    @GetMapping("/{id}")
    public String getStatistik(@PathVariable("id") Integer studentId, Model model) {

        System.out.println("Statistik wird geladen für Student ID: " + studentId);
        var statistik = statistikService.berechneFuerStudent(studentId);

        if (statistik.student() == null) {
            System.out.println("Keine Erfassung für Student gefunden.");
            return "redirect:/anwesenheit"; // Fallback falls keine Erfassung vorhanden
        }
        System.out.println("Statistik geladen: " + statistik);

        model.addAttribute("statistik", statistik);
        return "statistik";
    }
}
