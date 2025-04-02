package com.art.erfassung.controller;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.GruppeRepository;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/anwesenheit")
public class ErfassungController {

    private final StudentenService studentenService;
    private final ErfassungService erfassungService;
    private final StatusService statusService;
    private final GruppeService gruppeService;

    public ErfassungController(StudentenService studentenService, ErfassungService erfassungService,
                               StatusService statusService, GruppeService gruppeService) {
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
        this.statusService = statusService;
        this.gruppeService = gruppeService;
    }

    @GetMapping("/{gruppeId}")
    public String getStudentenListe(@PathVariable Integer gruppeId, Model model) {
        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
        List<Studenten> studentenListe = studentenService.findByGruppeId(gruppe.getId());
        List<Status> statusListe = statusService.findAll();
        model.addAttribute("studentenListe", studentenListe);
        model.addAttribute("statusListe", statusListe);
        model.addAttribute("gruppe", gruppe);
        return "anwesenheit";
    }

    @PostMapping("/speichern")
    public String speichernAnwesenheit(@RequestParam("studentenIdFront") List<Integer> studentenIds,
                                       @RequestParam("statusFront") List<Integer> statusIds,
                                       @RequestParam("kommentarFront") List<String> kommentare) {

        if (studentenIds.size() != statusIds.size() || studentenIds.size() != kommentare.size()) {
            throw new IllegalArgumentException("Listenlängen stimmen nicht überein");
        }

        LocalDate datum = LocalDate.now();
        Integer gruppeId = null;

        for (int i = 0; i < studentenIds.size(); i++) {
            Studenten student = studentenService.findOrThrow(studentenIds.get(i));
            Status status = statusService.findOrThrow(statusIds.get(i));
            String kommentar = kommentare.get(i);

            gruppeId = student.getGruppe().getId();

            Erfassung erfassung = erfassungService.findByStudentAndDate(student.getId(), datum)
                    .map(e -> {
                        e.setStatus(status);
                        e.setKommentar(kommentar);
                        return e;
                    })
                    .orElseGet(() -> new Erfassung(student, datum, status, kommentar));
            erfassungService.save(erfassung);
        }
        return (gruppeId != null)
                ? "redirect:/anwesenheit/" + gruppeId
                : "redirect:/gruppen";
    }
}
