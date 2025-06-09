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
        AnwesenheitForm form = getAnwesenheitForm(studentenListe);

        // Füge alle nötigen Model-Attribute hinzu
        model.addAttribute("anwesenheitForm", form);
        model.addAttribute("gruppe", gruppe);
        model.addAttribute("studentenListe", studentenListe);
        model.addAttribute("statusListe", statusService.findAll());
        return "anwesenheit";
    }

    private static AnwesenheitForm getAnwesenheitForm(List<Studenten> studentenListe) {
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
        return form;
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
}
