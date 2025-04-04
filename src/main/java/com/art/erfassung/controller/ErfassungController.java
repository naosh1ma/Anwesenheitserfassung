package com.art.erfassung.controller;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StatusService;
import com.art.erfassung.service.StudentenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
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
     * @param erfassungService  Service zur Verwaltung der Erfassungen
     * @param statusService     Service zur Verwaltung der Statusinformationen
     * @param gruppeService     Service zur Verwaltung der Gruppen
     */
    public ErfassungController(StudentenService studentenService, ErfassungService erfassungService,
                               StatusService statusService, GruppeService gruppeService) {
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
        this.statusService = statusService;
        this.gruppeService = gruppeService;
    }

    /**
     * Verarbeitet GET-Anfragen zum Abruf der Studentenliste einer bestimmten Gruppe.
     * <p>
     * Die Methode lädt die Gruppe anhand der übergebenen Gruppen-ID, ruft die zugehörigen
     * Studenten sowie alle verfügbaren Status ab und fügt diese als Attribute dem Model hinzu.
     * Das Model wird anschließend an die View "anwesenheit" weitergereicht.
     * </p>
     *
     * @param gruppeId die ID der Gruppe, für die die Studentenliste abgerufen werden soll
     * @param model    das Model, in das die Daten für die View eingefügt werden
     * @return den Namen der View, in diesem Fall "anwesenheit"
     */
    @GetMapping("/{gruppeId}")
    public String getStudentenListe(@PathVariable Integer gruppeId, Model model) {
        // Gruppe anhand der ID laden, oder eine Exception werfen, falls sie nicht existiert
        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
        // Liste der Studenten in der geladenen Gruppe abrufen
        List<Studenten> studentenListe = studentenService.findByGruppeId(gruppe.getId());
        // Liste aller verfügbaren Status abrufen
        List<Status> statusListe = statusService.findAll();
        // Hinzufügen der Daten zum Model, damit sie in der View verfügbar sind
        model.addAttribute("studentenListe", studentenListe);
        model.addAttribute("statusListe", statusListe);
        model.addAttribute("gruppe", gruppe);
        // Rückgabe des View-Namens
        return "anwesenheit";
    }

    /**
     * Verarbeitet POST-Anfragen zum Speichern der Anwesenheitsdaten.
     * <p>
     * Die Methode erhält Listen von Studenten-IDs, Status-IDs und Kommentaren, überprüft, ob die
     * Listenlängen übereinstimmen, und verarbeitet für jeden Studenten die Erfassung. Für den
     * aktuellen Tag wird entweder eine bestehende Erfassung aktualisiert oder eine neue Erfassung
     * erstellt und anschließend gespeichert.
     * </p>
     *
     * @param studentenIds Liste der Studenten-IDs, die anwesend sind
     * @param statusIds    Liste der Status-IDs, die den Anwesenheitsstatus repräsentieren
     * @param kommentare   Liste der Kommentare zu den Erfassungen
     * @return eine Weiterleitung zur entsprechenden View basierend auf der Gruppen-ID oder
     *         zur Gruppenübersicht, falls keine Gruppen-ID ermittelt werden konnte
     * @throws IllegalArgumentException wenn die Listenlängen der übergebenen Parameter nicht übereinstimmen
     */
    @PostMapping("/speichern")
    public String speichernAnwesenheit(@RequestParam("studentenIdFront") List<Integer> studentenIds,
                                       @RequestParam("statusFront") List<Integer> statusIds,
                                       @RequestParam("kommentarFront") List<String> kommentare) {

        // Überprüfen, ob alle übergebenen Listen die gleiche Länge haben
        if (studentenIds.size() != statusIds.size() || studentenIds.size() != kommentare.size()) {
            throw new IllegalArgumentException("Listenlängen stimmen nicht überein");
        }
        // Ermitteln des aktuellen Datums
        LocalDate datum = LocalDate.now();
        // Variable zur Speicherung der Gruppen-ID (wird anhand des ersten Studenten ermittelt)
        Integer gruppeId = null;
        // Iteration über alle Studenten-IDs, um die entsprechenden Erfassungen zu verarbeiten
        for (int i = 0; i < studentenIds.size(); i++) {
            // Laden des Studenten anhand der ID; wirft eine Exception, falls nicht gefunden
            Studenten student = studentenService.findOrThrow(studentenIds.get(i));
            // Laden des Status anhand der ID; wirft eine Exception, falls nicht gefunden
            Status status = statusService.findOrThrow(statusIds.get(i));
            // Kommentar für die Erfassung aus der Liste entnehmen
            String kommentar = kommentare.get(i);
            // Speichern der Gruppen-ID, basierend auf dem Studenten (alle Studenten gehören derselben Gruppe)
            gruppeId = student.getGruppe().getId();
            // Überprüfen, ob für den aktuellen Tag bereits eine Erfassung für den Studenten existiert.
            // Falls ja, wird diese aktualisiert, ansonsten wird eine neue Erfassung erstellt.
            Erfassung erfassung = erfassungService.findByStudentAndDate(student.getId(), datum)
                    .map(e -> {
                        // Aktualisieren des Status und Kommentars der vorhandenen Erfassung
                        e.setStatus(status);
                        e.setKommentar(kommentar);
                        return e;
                    }).orElseGet(() -> new Erfassung(student, datum, status, kommentar));
            // Speichern der Erfassung
            erfassungService.save(erfassung);
        }
        // Weiterleitung zur entsprechenden Seite basierend auf der ermittelten Gruppen-ID
        return (gruppeId != null) ? "redirect:/anwesenheit/" + gruppeId : "redirect:/gruppen";
    }
}
