package com.art.erfassung.controller;

import com.art.erfassung.dto.GruppeDTO;
import com.art.erfassung.mapper.GruppeMapper;
import com.art.erfassung.service.GruppeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller zur Verwaltung des Gruppen-Dashboards.
 * <p>
 * Alle Methoden in dieser Klasse betreffen das Dashboard, das unter "/dashboard" erreichbar ist.
 * </p>
 */
@Controller
@RequestMapping
public class GruppeController {

    // Service zur Verwaltung von Gruppen.
    private final GruppeService gruppeService;

    // Mapper zur Konvertierung zwischen Gruppe-Entitäten und DTOs.
    private final GruppeMapper gruppeMapper;

    @Autowired
    public GruppeController(GruppeService gruppeService, GruppeMapper gruppeMapper) {
        this.gruppeService = gruppeService;
        this.gruppeMapper = gruppeMapper;
    }

    /**
     * Zeigt das Dashboard mit der Liste aller Gruppen an.
     * <p>
     * Diese Methode verarbeitet GET-Anfragen an "/dashboard/gruppe". Es werden alle Gruppen
     * über den GruppeService abgerufen und dem Model hinzugefügt, damit sie in der View "gruppen"
     * dargestellt werden können.
     * </p>
     *
     * @param model das Model, das die Daten für die View enthält
     * @return den Namen der View "gruppen"
     */
    @GetMapping("/gruppen")
    public String showDashboard(Model model) {
        // Abrufen der Liste aller Gruppen und Konvertierung in DTOs
        List<GruppeDTO> gruppenList = gruppeMapper.toDTOList(gruppeService.findAll());
        // Hinzufügen der Gruppenliste zum Model, damit sie in der View verwendet werden kann
        model.addAttribute("gruppenListe", gruppenList);
        // Rückgabe des View-Namens "gruppen"
        return "gruppen";
    }
}
