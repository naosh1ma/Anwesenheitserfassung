package com.art.erfassung.controller;

import com.art.erfassung.dto.AnwesenheitDTO;
import com.art.erfassung.dto.AnwesenheitForm;
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

    private static final Logger logger = LoggerFactory.getLogger(ErfassungController.class);

    public ErfassungController(StudentenService studentenService, ErfassungService erfassungService,
                               StatusService statusService, GruppeService gruppeService) {
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
        this.statusService = statusService;
        this.gruppeService = gruppeService;
    }


    /**
     * Zeigt das Formular zur Anwesenheitserfassung für eine bestimmte Gruppe an.
     * <p>
     * Diese Methode führt folgende Schritte aus:
     * <ul>
     *   <li>Die Gruppe wird anhand der übergebenen Gruppen-ID abgefragt. Falls die Gruppe
     *       nicht existiert, wird eine Exception geworfen.</li>
     *   <li>Die Liste der Studenten, die der Gruppe zugeordnet sind, wird ermittelt.</li>
     *   <li>Ein {@code AnwesenheitsForm}-Objekt wird erstellt und für jeden Studenten ein
     *       {@code AnwesenheitsDTO} initialisiert. Dabei wird zumindest die Studenten-ID und
     *       optional der Name gesetzt.</li>
     *   <li>Die benötigten Model-Attribute (Formular, Gruppe, Studentenliste und Statusliste)
     *       werden dem Model hinzugefügt.</li>
     *   <li>Die Methode gibt den View-Namen "anwesenheit" zurück, sodass das entsprechende
     *       Thymeleaf-Template gerendert wird.</li>
     * </ul>
     * </p>
     *
     * @param gruppeId die ID der Gruppe, für die das Formular angezeigt werden soll
     * @param model    das Model, in das die Daten für die View eingefügt werden
     * @return den Namen des Views, hier "anwesenheit"
     */
    @GetMapping("/{gruppeId}")
    public String showAnwesenheitForm(@PathVariable Integer gruppeId, Model model) {
        // Gruppe anhand der ID laden, wobei findOrThrow() eine Exception wirft, falls die Gruppe nicht vorhanden ist.
        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
        // Alle Studenten der Gruppe abrufen.
        List<Studenten> studentenListe = studentenService.findByGruppeId(gruppeId);
        // Erstelle ein neues Formularobjekt, das die Erfassungsdaten kapselt.
        AnwesenheitForm form = new AnwesenheitForm();
        // Initialisiere die Liste der Einträge
        List<AnwesenheitDTO> eintraege = new ArrayList<>();
        // Für jeden Studenten wird ein entsprechendes DTO angelegt, in dem die Studenten-ID (und optional der Name) gesetzt wird.
        for (Studenten student : studentenListe) {
            AnwesenheitDTO dto = new AnwesenheitDTO();
            dto.setStudentenId(student.getId());
            // Optional: Setze den Studentennamen für die Anzeige, falls diese Information benötigt wird.
            dto.setStudentenName(student.getVorname() + " " + student.getName());
            // Weitere Felder (z. B. Ankunftszeit, Kommentar etc.) werden leer gelassen und im Formular ausgefüllt.
            eintraege.add(dto);
        }
        // Setze die Einträge im Formularobjekt.
        form.setEintraege(eintraege);

        // Füge alle nötigen Model-Attribute hinzu
        model.addAttribute("anwesenheitForm", form);
        model.addAttribute("gruppe", gruppe);
        model.addAttribute("studentenListe", studentenListe);
        model.addAttribute("statusListe", statusService.findAll());
        return "anwesenheit";
    }





    /**
     * Verarbeitet den POST-Request zum Speichern der Anwesenheitsdaten.
     * <p>
     * Diese Methode validiert die über das Formular empfangenen Daten. Falls Validierungsfehler vorliegen,
     * wird der Benutzer zurück zur Eingabeseite (View "anwesenheit") geleitet, und es wird eine Fehlermeldung im Model abgelegt.
     * Andernfalls werden die Anwesenheitsdaten aus dem Formular an den Service delegiert, der die Geschäftslogik
     * (zum Beispiel Verspätungsberechnung und Speicherung der Erfassungen) umsetzt. Nach erfolgreicher Verarbeitung
     * erfolgt eine Weiterleitung auf den entsprechenden View, basierend auf der ermittelten Gruppen-ID.
     * </p>
     *
     * @param form           Das validierte Formularobjekt, welches die Liste der Anwesenheitsdaten (DTOs) enthält.
     * @param bindingResult  Enthält das Ergebnis der Validierung des Formulars. Bei Fehlern werden diese hier festgehalten.
     * @param model          Das Model, in das Fehlermeldungen oder andere View-bezogene Attribute eingefügt werden.
     * @return               Ein Redirect-String, der den Benutzer entweder zur Gruppe ("/anwesenheit/{gruppeId}")
     *                       oder zur Gruppenübersicht ("/gruppen") weiterleitet.
     */
    @PostMapping("/speichern")
    public String speichernAnwesenheit(@Valid AnwesenheitForm form, BindingResult bindingResult, Model model) {
        // Logge den Start der Verarbeitung mit den übergebenen Form-Daten.
        logger.debug("speichernAnwesenheit() wurde aufgerufen mit Form-Daten: {}", form);
        // Überprüfe, ob Validierungsfehler vorliegen.
        if (bindingResult.hasErrors()) {
            // Protokolliere jeden Validierungsfehler.
            bindingResult.getAllErrors().forEach(error -> logger.error("Validierungsfehler: {}", error));
            // Füge eine allgemeine Fehlermeldung dem Model hinzu, um den Benutzer zu informieren.
            model.addAttribute("errorMessage", "Bitte überprüfen Sie Ihre Eingaben.");
            return "anwesenheit";
        }
        // Delegiere die Verarbeitung der Anwesenheitsdaten an die Service-Schicht.
        // Die Methode processAnwesenheiten() setzt alle notwendigen Geschäftsregeln um und gibt die zugehörige Gruppen-ID zurück.
        Integer gruppeId = erfassungService.erfassenAnwesenheiten(form.getEintraege());
        // Logge den erfolgreichen Abschluss der Verarbeitung.
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
