package com.art.erfassung.controller;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.repository.StatusRepository;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/liste")
public class ListeController {

    private final GruppeRepository gruppeRepository;
    private final StudentenRepository studentenRepository;
    private final ErfassungRepository erfassungRepository;
    private final StatusRepository statusRepository;

    public ListeController(GruppeRepository gruppeRepository, StudentenRepository studentenRepository,
                           ErfassungRepository erfassungRepository, StatusRepository statusRepository) {
        this.gruppeRepository = gruppeRepository;
        this.studentenRepository = studentenRepository;
        this.erfassungRepository = erfassungRepository;
        this.statusRepository = statusRepository;
    }

    @GetMapping("/{gruppenId}")
    public String anwesenheitAnzeigen(@PathVariable Integer gruppenId,
                                      @RequestParam(required = false) String monat,
                                      Model model) {

        Gruppe gruppe = gruppeRepository.findById(gruppenId).orElse(null);
        List<Studenten> studenten = studentenRepository.findByGruppeId(gruppe.getId());

        LocalDate monatStart = (monat != null) ? LocalDate.parse(monat + "-01") : LocalDate.now().withDayOfMonth(1);
        LocalDate monatEnde = monatStart.withDayOfMonth(monatStart.lengthOfMonth());

        List<Erfassung> erfassungen = erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(gruppe.getId(), monatStart, monatEnde);

        model.addAttribute("gruppe", gruppe);
        model.addAttribute("studenten", studenten);
        model.addAttribute("erfassungen", erfassungen);
        model.addAttribute("monat", monatStart.toString().substring(0, 7)); // YYYY-MM
        model.addAttribute("tageImMonat", monatStart.lengthOfMonth());

        return "anwesenheitsliste";
    }

    @PostMapping("/update/{id}")
    public String updateAnwesenheit(
            @PathVariable Integer id,
            @RequestParam Integer statusId,
            @RequestParam String kommentar) {

        Optional<Erfassung> optionalErfassung = erfassungRepository.findById(id);
        if (optionalErfassung.isPresent()) {
            Erfassung erfassung = optionalErfassung.get();
            Status neuerStatus = statusRepository.findById(statusId).orElse(null);
            if (neuerStatus != null) {
                erfassung.setStatus(neuerStatus);
                erfassung.setKommentar(kommentar);
                erfassungRepository.save(erfassung);
            }
        }
        return "redirect:/anwesenheitsliste/" + optionalErfassung.get().getStudenten().getGruppe().getId();
    }
}
