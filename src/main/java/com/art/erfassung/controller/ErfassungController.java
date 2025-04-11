package com.art.erfassung.controller;

import com.art.erfassung.dto.AnwesenheitsDTO;
import com.art.erfassung.dto.AnwesenheitsForm;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StatusService;
import com.art.erfassung.service.StudentenService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller zur Verwaltung der Anwesenheitserfassung.
 * <p>
 * Dieser Controller verarbeitet HTTP-Anfragen, die sich auf die Erfassung von Anwesenheitsdaten
 * beziehen. Er bietet Methoden zum Anzeigen der Studentenliste einer Gruppe sowie zum Speichern
 * von Anwesenheitsdaten.
 * </p>
 */
@Controller
@RequestMapping("/anwesenheit")
public class ErfassungController {

    // Service zur Verwaltung von Studenten
    private final StudentenService studentenService;
    // Service zur Verwaltung der Erfassungen
    private final ErfassungService erfassungService;
    // Service zur Verwaltung der Statusinformationen
    private final StatusService statusService;
    // Service zur Verwaltung der Gruppen
    private final GruppeService gruppeService;

    /**
     * Konstruktor zur Initialisierung des ErfassungControllers mit den benötigten Services.
     *
     * @param studentenService Service zur Verwaltung von Studenten
     * @param erfassungService Service zur Verwaltung der Erfassungen
     * @param statusService    Service zur Verwaltung der Statusinformationen
     * @param gruppeService    Service zur Verwaltung der Gruppen
     */
    public ErfassungController(StudentenService studentenService, ErfassungService erfassungService,
                               StatusService statusService, GruppeService gruppeService) {
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
        this.statusService = statusService;
        this.gruppeService = gruppeService;
    }



    @GetMapping("/{gruppeId}")
    public String showAnwesenheitForm(@PathVariable Integer gruppeId, Model model) {
        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
        List<Studenten> studentenListe = studentenService.findByGruppeId(gruppeId);
        // Erstelle und initialisiere ErfassungForm
        AnwesenheitsForm form = new AnwesenheitsForm();
        List<AnwesenheitsDTO> eintraege = new ArrayList<>();
        for (Studenten student : studentenListe) {
            AnwesenheitsDTO dto = new AnwesenheitsDTO();
            dto.setStudentenId(student.getId());
            // Optional: Falls dein DTO einen Studentennamen enthält, kannst du ihn hier setzen:
            dto.setStudentenName(student.getVorname() + " " + student.getName());
            // Weitere Felder bleiben leer und werden im Formular ausgefüllt.
            eintraege.add(dto);
        }
        form.setEintraege(eintraege);

        // Füge alle nötigen Model-Attribute hinzu
        model.addAttribute("erfassungForm", form);
        model.addAttribute("gruppe", gruppe);
        model.addAttribute("studentenListe", studentenListe);
        model.addAttribute("statusListe", statusService.findAll());
        return "anwesenheit";
    }




    private static final Logger logger = LoggerFactory.getLogger(ErfassungController.class);

    @PostMapping("/speichern")
    public String speichernAnwesenheit(@Valid AnwesenheitsForm form, BindingResult bindingResult, Model model) {
        logger.debug("speichernAnwesenheit() wurde aufgerufen mit Form-Daten: {}", form);
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> logger.error("Validierungsfehler: {}", error));
            model.addAttribute("errorMessage", "Bitte überprüfen Sie Ihre Eingaben.");
            return "anwesenheit";
        }

        Integer gruppeId = erfassungService.processAnwesenheiten(form.getEintraege());
        logger.info("Anwesenheit für Gruppe {} wurde erfolgreich verarbeitet.", gruppeId);
        return (gruppeId != null) ? "redirect:/anwesenheit/" + gruppeId : "redirect:/gruppen";
    }












//    /**
//     * Verarbeitet GET-Anfragen zum Abruf der Studentenliste einer bestimmten Gruppe.
//     * <p>
//     * Die Methode lädt die Gruppe anhand der übergebenen Gruppen-ID, ruft die zugehörigen
//     * Studenten sowie alle verfügbaren Status ab und fügt diese als Attribute dem Model hinzu.
//     * Das Model wird anschließend an die View "anwesenheit" weitergereicht.
//     * </p>
//     *
//     * @param gruppeId die ID der Gruppe, für die die Studentenliste abgerufen werden soll
//     * @param model    das Model, in das die Daten für die View eingefügt werden
//     * @return den Namen der View, in diesem Fall "anwesenheit"
//     */
//    @GetMapping("/{gruppeId}")
//    public String getStudentenListe(@PathVariable Integer gruppeId, Model model) {
//        // Gruppe anhand der ID laden, oder eine Exception werfen, falls sie nicht existiert
//        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
//        // Liste der Studenten in der geladenen Gruppe abrufen
//        List<Studenten> studentenListe = studentenService.findByGruppeId(gruppe.getId());
//        // Liste aller verfügbaren Status abrufen
//        List<Status> statusListe = statusService.findAll();
//        // Hinzufügen der Daten zum Model, damit sie in der View verfügbar sind
//        model.addAttribute("studentenListe", studentenListe);
//        model.addAttribute("statusListe", statusListe);
//        model.addAttribute("gruppe", gruppe);
//        model.addAttribute("anwesenheitForm", new ErfassungForm());
//        // Rückgabe des View-Namens
//        return "anwesenheit";
//    }


//    /**
//     * Verarbeitet POST-Anfragen zum Speichern der Anwesenheitsdaten.
//     * <p>
//     * Die Methode erhält Listen von Studenten-IDs, Status-IDs und Kommentaren, überprüft, ob die
//     * Listenlängen übereinstimmen, und verarbeitet für jeden Studenten die Erfassung. Für den
//     * aktuellen Tag wird entweder eine bestehende Erfassung aktualisiert oder eine neue Erfassung
//     * erstellt und anschließend gespeichert.
//     * </p>
//     *
//     * @param studentenIds  Liste der Studenten-IDs, die anwesend sind
//     * @param statusIds     Liste der Status-IDs, die den Anwesenheitsstatus repräsentieren
//     * @param kommentarList Liste der Kommentare zu den Erfassungen
//     * @return eine Weiterleitung zur entsprechenden View basierend auf der Gruppen-ID oder
//     * zur Gruppenübersicht, falls keine Gruppen-ID ermittelt werden konnte
//     * @throws IllegalArgumentException wenn die Listenlängen der übergebenen Parameter nicht übereinstimmen
//     */
//    @PostMapping("/speichern")
//    public String speichernAnwesenheit(@RequestParam("studentenIdFront") List<Integer> studentenIds,
//                                       @RequestParam("statusFront") List<Integer> statusIds,
//                                       @RequestParam("ankunftszeit") List<String> ankunftsZeitenList,
//                                       @RequestParam("verlassen_um") List<String> verlassenZeitenList,
//                                       @RequestParam("kommentarFront") List<String> kommentarList) {
//
//        // Überprüfen, ob alle übergebenen Listen die gleiche Länge haben
//        if (studentenIds.size() != statusIds.size() || studentenIds.size() != kommentarList.size()) {
//            throw new IllegalArgumentException("Listenlängen stimmen nicht überein");
//        }
//        // Ermitteln des aktuellen Datums
//        LocalDate datum = LocalDate.now();
//        // Erwartete Ankunftszeit (z. B. 08:00 Uhr)
//        LocalTime expectedTime = LocalTime.of(8, 0);
//        // Variable zur Speicherung der Gruppen-ID (wird anhand des ersten Studenten ermittelt)
//        Integer gruppeId = null;
//        // Liste zur Sammlung aller zu speichernden Erfassungen (Batch-Verarbeitung)
//        List<Erfassung> erfassungenToSave = new ArrayList<>();
//        // Verarbeitung jedes Eintrags
//        for (int i = 0; i < studentenIds.size(); i++) {
//            // Laden des Studenten anhand der ID; wirft eine Exception, wenn der Student nicht gefunden wird
//            Studenten student = studentenService.findOrThrow(studentenIds.get(i));
//            // Laden des Status anhand der ID; wirft eine Exception, wenn der Status nicht gefunden wird
//            Status status = statusService.findOrThrow(statusIds.get(i));
//            String kommentar = kommentarList.get(i);
//            // Bestimmen der Gruppen-ID anhand des Studenten (alle Einträge sollten derselben Gruppe angehören)
//            gruppeId = student.getGruppe().getId();
//            // Verarbeitung der Ankunftszeit: Falls vorhanden und später als 08:00, berechne Verspätung
//            String ankunftStr = ankunftsZeitenList.get(i);
//            if (ankunftStr != null && !ankunftStr.isEmpty()) {
//                LocalTime ankunftszeit = LocalTime.parse(ankunftStr);
//                // Wenn die Ankunftszeit nach 08:00 liegt, berechne die Verspätung in Minuten
//                if (ankunftszeit.isAfter(expectedTime)) {
//                    long delayMinutes = java.time.temporal.ChronoUnit.MINUTES.between(expectedTime, ankunftszeit);
//                    // Kommentar um Verspätungsinformation ergänzen, ggf. bestehender Kommentar anhängen
//                    kommentar = (kommentar != null && !kommentar.isEmpty())
//                            ? kommentar + " | Verspätung: " + delayMinutes + " Minuten"
//                            : "Verspätung: " + delayMinutes + " Minuten";
//                }
//            }
//            final String finalKommentar = kommentar;
//            // Überprüfen, ob für den aktuellen Tag bereits eine Erfassung existiert, und diese gegebenenfalls aktualisieren
//            Erfassung erfassung = erfassungService.findByStudentAndDate(student.getId(), datum)
//                    .map(e -> {
//                        e.setStatus(status);
//                        e.setKommentar(finalKommentar);
//                        return e;
//                    })
//                    .orElseGet(() -> new Erfassung(student, datum, status, finalKommentar));
//            // Hinzufügen der Erfassung zur Batch-Liste
//            erfassungenToSave.add(erfassung);
//        }
//        // Speichern aller Erfassungen in einem Batch-Vorgang
//        erfassungService.saveAll(erfassungenToSave);
//        // Weiterleitung basierend auf der Gruppen-ID oder zu den Gruppen, falls keine Gruppen-ID ermittelt wurde
//        return (gruppeId != null) ? "redirect:/anwesenheit/" + gruppeId : "redirect:/gruppen";
//    }

}
