package com.art.erfassung.controller;

import com.art.erfassung.dto.ErfassungDTO;
import com.art.erfassung.dto.ErfassungForm;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StatusService;
import com.art.erfassung.service.StudentenService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
public class ErfassungController{

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
     *   <li>Ein {@code ErfassungForm}-Objekt wird erstellt und für jeden Studenten ein
     *       {@code ErfassungDTO} initialisiert, das mit dem Status "Anwesend" vorbelegt ist.</li>
     *   <li>Die benötigten Model-Attribute (Formular, Gruppe und Statusliste)
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
        // Status "Anwesend" laden, mit dem jeder Student vorbelegt wird.
        Status anwesend = statusService.findAnwesend();
        // Erstelle ein neues Formularobjekt, das die Erfassungsdaten kapselt.
        ErfassungForm form = getErfassungForm(studentenListe, anwesend.getId());

        // Füge alle nötigen Model-Attribute hinzu
        model.addAttribute("anwesenheitForm", form);
        addFormAttributes(model, gruppe, anwesend);
        return "anwesenheit";
    }

    private static ErfassungForm getErfassungForm(List<Studenten> studentenListe, Integer anwesendStatusId) {
        ErfassungForm form = new ErfassungForm();
        // Initialisiere die Liste der Einträge
        List<ErfassungDTO> eintraege = new ArrayList<>();
        // Für jeden Studenten wird ein entsprechendes DTO angelegt, in dem die Studenten-ID und der Name gesetzt werden.
        for (Studenten student : studentenListe) {
            ErfassungDTO dto = new ErfassungDTO();
            dto.setStudentenId(student.getId());
            dto.setStudentenName(student.getVorname() + " " + student.getName());
            // Standardmäßig ist jeder Student anwesend.
            dto.setStatusId(anwesendStatusId);
            // Weitere Felder (z. B. Ankunftszeit, Kommentar etc.) werden leer gelassen und im Formular ausgefüllt.
            eintraege.add(dto);
        }
        // Setze die Einträge im Formularobjekt.
        form.setEintraege(eintraege);
        return form;
    }

    /**
     * Fügt die Model-Attribute hinzu, die das Template "anwesenheit" zusätzlich zum Formular benötigt.
     */
    private void addFormAttributes(Model model, Gruppe gruppe, Status anwesend) {
        model.addAttribute("gruppe", gruppe);
        model.addAttribute("statusListe", statusService.findAll());
        model.addAttribute("anwesendStatusId", anwesend.getId());
    }

    /**
     * Zeigt das abgeschickte Formular erneut an, z. B. nach Validierungsfehlern.
     * Die Studentennamen werden nicht mitgesendet und deshalb aus der Datenbank ergänzt.
     */
    private String showFormAgain(Integer gruppeId, ErfassungForm form, Model model) {
        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
        Map<Integer, String> namen = studentenService.findByGruppeId(gruppeId).stream()
                .collect(Collectors.toMap(Studenten::getId, s -> s.getVorname() + " " + s.getName()));
        if (form.getEintraege() == null) {
            form.setEintraege(new ArrayList<>());
        }
        form.getEintraege().removeIf(Objects::isNull);
        form.getEintraege().forEach(eintrag -> eintrag.setStudentenName(namen.get(eintrag.getStudentenId())));
        addFormAttributes(model, gruppe, statusService.findAnwesend());
        return "anwesenheit";
    }

    /**
     * Verarbeitet den POST-Request zum Speichern der Anwesenheitsdaten.
     * <p>
     * Diese Methode validiert die über das Formular empfangenen Daten. Falls Validierungsfehler vorliegen,
     * wird das Formular mit den eingegebenen Daten und einer Fehlermeldung erneut angezeigt.
     * Andernfalls werden die Anwesenheitsdaten aus dem Formular an den Service delegiert, der die Geschäftslogik
     * (zum Beispiel Verspätungsberechnung und Speicherung der Erfassungen) umsetzt. Nach erfolgreicher Verarbeitung
     * erfolgt eine Weiterleitung zurück zum Formular der Gruppe.
     * </p>
     *
     * @param gruppeId       Die ID der Gruppe, für die die Anwesenheit erfasst wird.
     * @param form           Das validierte Formularobjekt, welches die Liste der Anwesenheitsdaten (DTOs) enthält.
     * @param bindingResult  Enthält das Ergebnis der Validierung des Formulars. Bei Fehlern werden diese hier festgehalten.
     * @param model          Das Model, in das Fehlermeldungen oder andere View-bezogene Attribute eingefügt werden.
     * @return               Ein Redirect auf "/anwesenheit/{gruppeId}" oder bei Fehlern der View "anwesenheit".
     */
    @PostMapping("/{gruppeId}/speichern")
    public String speichernAnwesenheit(@PathVariable Integer gruppeId,
                                       @Valid @ModelAttribute("anwesenheitForm") ErfassungForm form,
                                       BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Logge den Start der Verarbeitung mit den übergebenen Form-Daten.
        logger.debug("speichernAnwesenheit() wurde aufgerufen mit Form-Daten: {}", form);

        // Überprüfe, ob Validierungsfehler vorliegen.
        if (bindingResult.hasErrors()) {
            // Protokolliere jeden Validierungsfehler.
            bindingResult.getAllErrors().forEach(error -> logger.warn("Validierungsfehler: {}", error));

            // Sammle die Fehlermeldungen. Bei Konvertierungsfehlern enthält die Standardmeldung den
            // eingegebenen Wert, daher wird stattdessen eine eigene Meldung verwendet.
            List<String> errorDetails = bindingResult.getAllErrors().stream()
                    .map(error -> error instanceof FieldError fieldError && fieldError.isBindingFailure()
                            ? "Ungültiger Wert für " + fieldError.getField()
                            : error.getDefaultMessage())
                    .distinct()
                    .toList();

            model.addAttribute("errorMessage", "Bitte korrigieren Sie folgende Fehler:");
            model.addAttribute("errorDetails", errorDetails);
            model.addAttribute("errorType", "validation");
            return showFormAgain(gruppeId, form, model);
        }

        try {
            // Delegiere die Verarbeitung der Anwesenheitsdaten an die Service-Schicht.
            erfassungService.erfassenAnwesenheiten(form.getEintraege());

            // Logge den erfolgreichen Abschluss der Verarbeitung.
            logger.info("Anwesenheit für Gruppe {} wurde erfolgreich verarbeitet.", gruppeId);

            // Erfolgsmeldung für den Benutzer
            redirectAttributes.addFlashAttribute("successMessage",
                "Anwesenheitsdaten wurden erfolgreich gespeichert!");

            return "redirect:/anwesenheit/" + gruppeId;

        } catch (Exception e) {
            logger.error("Fehler beim Speichern der Anwesenheitsdaten: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Fehler beim Speichern der Daten. Bitte versuchen Sie es erneut.");
            model.addAttribute("errorType", "database");
            return showFormAgain(gruppeId, form, model);
        }
    }
}
