package com.art.erfassung.controller;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.StatusRepository;
import com.art.erfassung.repository.StudentenRepository;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StatusService;
import com.art.erfassung.service.StudentenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/liste")
public class ListeController {

    private final GruppeService gruppeService;
    private final StudentenService studentenService;
    private final ErfassungService erfassungService;

    public ListeController(GruppeService gruppeService, StudentenService studentenService,
                           ErfassungService erfassungService) {
        this.gruppeService = gruppeService;
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
    }

    @GetMapping("/{gruppenId}")
    public String anwesenheitAnzeigen(@PathVariable Integer gruppenId,
                                      @RequestParam(required = false) String monat,
                                      Model model) {

        Gruppe gruppe = gruppeService.findOrThrow(gruppenId);
        List<Studenten> studenten = studentenService.findByGruppeId(gruppe.getId());

        LocalDate monatStart = (monat != null) ? LocalDate.parse(monat + "-01") : LocalDate.now().withDayOfMonth(1);
        LocalDate monatEnde = monatStart.withDayOfMonth(monatStart.lengthOfMonth());

        List<Erfassung> erfassungen = erfassungService.getByGruppeUndMonat(gruppe.getId(), monatStart, monatEnde);

        model.addAttribute("gruppe", gruppe);
        model.addAttribute("studenten", studenten);
        model.addAttribute("erfassungen", erfassungen);
        model.addAttribute("monat", monatStart.toString().substring(0, 7));
        model.addAttribute("tageImMonat", monatStart.lengthOfMonth());

        return "anwesenheitsliste";
    }

    @PostMapping("/update/{id}")
    public String updateAnwesenheit(@PathVariable Integer id,
                                    @RequestParam Integer statusId,
                                    @RequestParam String kommentar) {

        erfassungService.updateErfassung(id, statusId, kommentar);
        Gruppe gruppe = erfassungService.findById(id).orElseThrow().getStudenten().getGruppe();

        return "redirect:/anwesenheitsliste/" + gruppe.getId();
    }
}
