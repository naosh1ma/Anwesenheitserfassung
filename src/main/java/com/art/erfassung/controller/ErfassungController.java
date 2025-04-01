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
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/anwesenheit")
public class ErfassungController {

    private final StudentenRepository studentenRepository;
    private final ErfassungRepository erfassungRepository;
    private final StatusRepository statusRepository;
    private final GruppeRepository gruppeRepository;

    public ErfassungController(StudentenRepository studentenRepository, ErfassungRepository erfassungRepository,
                               StatusRepository statusRepository, GruppeRepository gruppeRepository) {
        this.studentenRepository = studentenRepository;
        this.erfassungRepository = erfassungRepository;
        this.statusRepository = statusRepository;
        this.gruppeRepository = gruppeRepository;
    }

    @GetMapping("/{gruppeId}")
    public String getStudentenListe(@PathVariable Integer gruppeId, Model model) {
        Gruppe gruppe = gruppeRepository.findById(gruppeId).orElse(null);
        if (gruppe == null) {
            return "gruppen"; // Falls die Gruppe nicht existiert
        }
        List<Studenten> studentenListe = studentenRepository.findByGruppeId(gruppe.getId());
        List<Status> statusListe = statusRepository.findAll(); // Status-Liste für Dropdown
        model.addAttribute("studentenListe", studentenListe);
        model.addAttribute("statusListe", statusListe);
        model.addAttribute("gruppe", gruppe);
        return "anwesenheit";
    }

    @PostMapping("/speichern")
    public String speichernAnwesenheit(@RequestParam("studentenId") List<Integer> studentenIds,
                                       @RequestParam("status") List<Integer> statusIds,
                                       @RequestParam("kommentar") List<String> kommentare) {

        LocalDate datum = LocalDate.now();
        for (int i = 0; i < studentenIds.size(); i++) {
            Studenten student = studentenRepository.findById(studentenIds.get(i)).orElse(null);
            Status status = statusRepository.findById(statusIds.get(i)).orElse(null);
            String kommentar = kommentare.get(i);
            if (student != null && status != null) {
                Erfassung erfassung = new Erfassung(student, datum, status, kommentar);
                erfassungRepository.save(erfassung);
            }
        }
        return "redirect:/anwesenheit/" + studentenIds.get(0); // Zurück zur Gruppe
    }

//    @GetMapping("/liste/{gruppenId}")
//    public String anwesenheitAnzeigen(@PathVariable Integer gruppenId,
//                                      @RequestParam(required = false) String monat,
//                                      Model model) {
//        Gruppe gruppe = gruppeRepository.findById(gruppenId).orElse(null);
//        List<Studenten> studenten = studentenRepository.findByGruppeId(gruppe.getId());
//        LocalDate monatStart = (monat != null) ? LocalDate.parse(monat + "-01") : LocalDate.now().withDayOfMonth(1);
//        LocalDate monatEnde = monatStart.withDayOfMonth(monatStart.lengthOfMonth());
//        List<Erfassung> erfassungen = erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(gruppe.getId(), monatStart, monatEnde);
//        model.addAttribute("gruppe", gruppe);
//        model.addAttribute("studenten", studenten);
//        model.addAttribute("erfassungen", erfassungen);
//        model.addAttribute("monat", monatStart.toString().substring(0, 7)); // YYYY-MM
//        model.addAttribute("tageImMonat", monatStart.lengthOfMonth());
//        return "anwesenheitsliste";
//    }
//
//
//    @PostMapping("/liste/update/{id}")
//    public String updateAnwesenheit(
//            @PathVariable Integer id,
//            @RequestParam Integer statusId,
//            @RequestParam String kommentar) {
//        Optional<Erfassung> optionalErfassung = erfassungRepository.findById(id);
//        if (optionalErfassung.isPresent()) {
//            Erfassung erfassung = optionalErfassung.get();
//            Status neuerStatus = statusRepository.findById(statusId).orElse(null);
//            if (neuerStatus != null) {
//                erfassung.setStatus(neuerStatus);
//                erfassung.setKommentar(kommentar);
//                erfassungRepository.save(erfassung);
//            }
//        }
//        return "redirect:/anwesenheitsliste/" + optionalErfassung.get().getStudenten().getGruppe().getId();
//    }
}
