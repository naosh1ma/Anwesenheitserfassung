package com.art.erfassung.controller;

import com.art.erfassung.dto.GruppeDTO;
import com.art.erfassung.mapper.GruppeMapper;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.service.StudentenService;
import com.art.erfassung.service.ZugriffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller für die Gruppenübersicht.
 * <p>
 * Zeigt die Gruppen des angemeldeten Benutzers mit der Anzahl ihrer aktiven Studenten unter "/gruppen" an:
 * alle Gruppen für Administratoren, die zugewiesenen Gruppen für Lehrer.
 * </p>
 */
@Controller
@RequestMapping
public class GruppeController {

    // Service, der die für den Benutzer sichtbaren Gruppen liefert.
    private final ZugriffService zugriffService;

    // Service zur Abfrage der Studenten einer Gruppe.
    private final StudentenService studentenService;

    // Mapper zur Konvertierung zwischen Gruppe-Entitäten und DTOs.
    private final GruppeMapper gruppeMapper;

    @Autowired
    public GruppeController(ZugriffService zugriffService, StudentenService studentenService, GruppeMapper gruppeMapper) {
        this.zugriffService = zugriffService;
        this.studentenService = studentenService;
        this.gruppeMapper = gruppeMapper;
    }

    /**
     * Zeigt die Gruppenübersicht mit den Gruppen an, die der Benutzer sehen darf.
     * <p>
     * Die Gruppen werden zusammen mit der Anzahl ihrer aktiven Studenten dem Model hinzugefügt.
     * </p>
     *
     * @param authentication der angemeldete Benutzer
     * @param model          das Model, das die Daten für die View enthält
     * @return den Namen der View "gruppen"
     */
    @GetMapping("/gruppen")
    public String showDashboard(Authentication authentication, Model model) {
        List<Gruppe> gruppen = zugriffService.sichtbareGruppen(authentication);
        // Anzahl der aktiven Studenten je Gruppe
        Map<Integer, Integer> studentenAnzahl = new HashMap<>();
        for (Gruppe gruppe : gruppen) {
            studentenAnzahl.put(gruppe.getId(), studentenService.findAktiveByGruppeId(gruppe.getId()).size());
        }
        List<GruppeDTO> gruppenList = gruppeMapper.toDTOList(gruppen);
        model.addAttribute("gruppenListe", gruppenList);
        model.addAttribute("studentenAnzahl", studentenAnzahl);
        return "gruppen";
    }
}
