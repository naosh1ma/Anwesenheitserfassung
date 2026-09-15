package com.art.erfassung.controller;

import com.art.erfassung.dto.ErfassungDTO;
import com.art.erfassung.dto.ErfassungForm;
import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StatusService;
import com.art.erfassung.service.StudentenService;
import com.art.erfassung.service.ZugriffService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung der Anwesenheitserfassung.
 * <p>
 * Dieser Controller verarbeitet HTTP-Anfragen, die sich auf die Erfassung von Anwesenheitsdaten
 * beziehen. Er bietet Methoden zum Anzeigen der Studentenliste einer Gruppe sowie zum Speichern
 * von Anwesenheitsdaten, für heute oder nachträglich für einen vergangenen Tag.
 * Lehrer können nur Gruppen öffnen, die ihnen zugewiesen sind.
 * </p>
 */
@Controller
@RequestMapping("/anwesenheit")
public class ErfassungController{

    // Anzeigeformat für Uhrzeiten im Formular
    private static final DateTimeFormatter ZEIT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    // Datumsformat in Meldungen
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Service zur Verwaltung von Studenten
    private final StudentenService studentenService;
    // Service zur Verwaltung der Erfassungen
    private final ErfassungService erfassungService;
    // Service zur Verwaltung der Statusinformationen
    private final StatusService statusService;
    // Service zur Verwaltung der Gruppen
    private final GruppeService gruppeService;
    // Service zur Prüfung, ob der Benutzer die Gruppe sehen darf
    private final ZugriffService zugriffService;

    // Unterrichtsbeginn (HH:mm), ab dem das Formular eine Ankunftszeit als Verspätung anzeigt
    private final String unterrichtsbeginn;

    private static final Logger logger = LoggerFactory.getLogger(ErfassungController.class);

    public ErfassungController(StudentenService studentenService, ErfassungService erfassungService,
                               StatusService statusService, GruppeService gruppeService, ZugriffService zugriffService,
                               @Value("${app.unterricht.beginn:08:00}") String unterrichtsbeginn) {
        this.studentenService = studentenService;
        this.erfassungService = erfassungService;
        this.statusService = statusService;
        this.gruppeService = gruppeService;
        this.zugriffService = zugriffService;
        this.unterrichtsbeginn = LocalTime.parse(unterrichtsbeginn.trim(), DateTimeFormatter.ofPattern("H:mm"))
                .format(ZEIT_FORMAT);
    }

    /**
     * Zeigt das Formular zur Anwesenheitserfassung für eine Gruppe und einen Tag an.
     * <p>
     * Diese Methode führt folgende Schritte aus:
     * <ul>
     *   <li>Prüft, ob der Benutzer die Gruppe sehen darf, und lädt sie. Falls die Gruppe
     *       nicht existiert, wird eine Exception geworfen.</li>
     *   <li>Ohne Datum wird der heutige Tag angezeigt. Zukünftige Tage sind nicht erlaubt und
     *       führen zurück zum heutigen Tag.</li>
     *   <li>Ermittelt die Studenten, die an diesem Tag aktiv waren.</li>
     *   <li>Ein {@code ErfassungForm}-Objekt wird erstellt. Wurde für einen Studenten an diesem Tag bereits eine
     *       Erfassung gespeichert, werden deren Werte übernommen, damit erneutes Speichern nichts überschreibt.
     *       Andernfalls ist der Student mit dem Status "Anwesend" vorbelegt.</li>
     * </ul>
     * </p>
     *
     * @param gruppeId       die ID der Gruppe, für die das Formular angezeigt werden soll
     * @param datum          (optional) der Tag im Format JJJJ-MM-TT; ohne Angabe heute
     * @param authentication der angemeldete Benutzer
     * @param model          das Model, in das die Daten für die View eingefügt werden
     * @return den Namen des Views "anwesenheit" oder ein Redirect auf heute bei einem zukünftigen Tag
     */
    @GetMapping("/{gruppeId}")
    public String showAnwesenheitForm(@PathVariable Integer gruppeId,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum,
                                      Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        zugriffService.pruefeGruppe(gruppeId, authentication);
        // Gruppe anhand der ID laden, wobei findOrThrow() eine Exception wirft, falls die Gruppe nicht vorhanden ist.
        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
        LocalDate tag = datum != null ? datum : LocalDate.now();
        if (tag.isAfter(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Für zukünftige Tage kann noch keine Anwesenheit erfasst werden.");
            return "redirect:/anwesenheit/" + gruppeId;
        }
        // Alle Studenten, die an diesem Tag aktiv waren (für deaktivierte Studenten wird nichts mehr erfasst).
        List<Studenten> studentenListe = studentenService.findAktiveAmTag(gruppeId, tag);
        // Status "Anwesend" laden, mit dem jeder Student ohne Erfassung vorbelegt wird.
        Status anwesend = statusService.findAnwesend();
        // An diesem Tag bereits gespeicherte Erfassungen der Gruppe, nach Studenten-ID.
        Map<Integer, Erfassung> erfassungenDesTages = erfassungService.findByGruppeUndMonat(gruppeId, tag, tag).stream()
                .collect(Collectors.toMap(e -> e.getStudenten().getId(), Function.identity(), (erste, zweite) -> erste));
        // Erstelle ein neues Formularobjekt, das die Erfassungsdaten kapselt.
        ErfassungForm form = getErfassungForm(studentenListe, anwesend.getId(), erfassungenDesTages);

        // Füge alle nötigen Model-Attribute hinzu
        model.addAttribute("anwesenheitForm", form);
        addFormAttributes(model, gruppe, anwesend, tag);
        return "anwesenheit";
    }

    private static ErfassungForm getErfassungForm(List<Studenten> studentenListe, Integer anwesendStatusId,
                                                  Map<Integer, Erfassung> erfassungenDesTages) {
        ErfassungForm form = new ErfassungForm();
        // Initialisiere die Liste der Einträge
        List<ErfassungDTO> eintraege = new ArrayList<>();
        // Für jeden Studenten wird ein entsprechendes DTO angelegt, in dem die Studenten-ID und der Name gesetzt werden.
        for (Studenten student : studentenListe) {
            ErfassungDTO dto = new ErfassungDTO();
            dto.setStudentenId(student.getId());
            dto.setStudentenName(student.getVorname() + " " + student.getName());
            Erfassung erfassung = erfassungenDesTages.get(student.getId());
            if (erfassung != null) {
                // Bereits gespeicherte Werte übernehmen.
                dto.setStatusId(erfassung.getStatus().getId());
                dto.setAnkunftszeit(formatZeit(erfassung.getAnkunftszeit()));
                dto.setVerlassenUm(formatZeit(erfassung.getVerlassenUm()));
                dto.setKommentar(erfassung.getKommentar());
            } else {
                // Standardmäßig ist jeder Student anwesend.
                dto.setStatusId(anwesendStatusId);
            }
            eintraege.add(dto);
        }
        // Setze die Einträge im Formularobjekt.
        form.setEintraege(eintraege);
        return form;
    }

    private static String formatZeit(LocalTime zeit) {
        return zeit == null ? null : zeit.format(ZEIT_FORMAT);
    }

    /**
     * Fügt die Model-Attribute hinzu, die das Template "anwesenheit" zusätzlich zum Formular benötigt,
     * darunter den angezeigten Tag und die Nachbartage für die Tagesnavigation.
     */
    private void addFormAttributes(Model model, Gruppe gruppe, Status anwesend, LocalDate tag) {
        LocalDate heute = LocalDate.now();
        model.addAttribute("gruppe", gruppe);
        model.addAttribute("statusListe", statusService.findAll());
        model.addAttribute("anwesendStatusId", anwesend.getId());
        model.addAttribute("unterrichtsbeginn", unterrichtsbeginn);
        model.addAttribute("datum", tag);
        model.addAttribute("heute", heute);
        model.addAttribute("istHeute", tag.equals(heute));
        model.addAttribute("vorherigerTag", tag.minusDays(1));
        // Kein Link auf morgen: für zukünftige Tage wird nichts erfasst
        model.addAttribute("naechsterTag", tag.isBefore(heute) ? tag.plusDays(1) : null);
    }

    /**
     * Zeigt das abgeschickte Formular erneut an, z. B. nach Validierungsfehlern.
     * Die Studentennamen werden nicht mitgesendet und deshalb aus der Datenbank ergänzt.
     */
    private String showFormAgain(Integer gruppeId, LocalDate tag, ErfassungForm form, Model model) {
        Gruppe gruppe = gruppeService.findOrThrow(gruppeId);
        Map<Integer, String> namen = studentenService.findByGruppeId(gruppeId).stream()
                .collect(Collectors.toMap(Studenten::getId, s -> s.getVorname() + " " + s.getName()));
        if (form.getEintraege() == null) {
            form.setEintraege(new ArrayList<>());
        }
        form.getEintraege().removeIf(Objects::isNull);
        form.getEintraege().forEach(eintrag -> eintrag.setStudentenName(namen.get(eintrag.getStudentenId())));
        addFormAttributes(model, gruppe, statusService.findAnwesend(), tag);
        return "anwesenheit";
    }

    /**
     * Verarbeitet den POST-Request zum Speichern der Anwesenheitsdaten.
     * <p>
     * Diese Methode validiert die über das Formular empfangenen Daten. Falls Validierungsfehler vorliegen,
     * wird das Formular mit den eingegebenen Daten und einer Fehlermeldung erneut angezeigt.
     * Andernfalls werden die Anwesenheitsdaten aus dem Formular an den Service delegiert, der sie prüft
     * und in einer Transaktion speichert. Nach erfolgreicher Verarbeitung
     * erfolgt eine Weiterleitung zurück zum Formular der Gruppe für denselben Tag.
     * </p>
     *
     * @param gruppeId       Die ID der Gruppe, für die die Anwesenheit erfasst wird.
     * @param datum          (optional) Der Tag, für den gespeichert wird; ohne Angabe heute.
     * @param form           Das validierte Formularobjekt, welches die Liste der Anwesenheitsdaten (DTOs) enthält.
     * @param bindingResult  Enthält das Ergebnis der Validierung des Formulars. Bei Fehlern werden diese hier festgehalten.
     * @param authentication Der angemeldete Benutzer.
     * @param model          Das Model, in das Fehlermeldungen oder andere View-bezogene Attribute eingefügt werden.
     * @return               Ein Redirect auf das Formular der Gruppe oder bei Fehlern der View "anwesenheit".
     */
    @PostMapping("/{gruppeId}/speichern")
    public String speichernAnwesenheit(@PathVariable Integer gruppeId,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum,
                                       @Valid @ModelAttribute("anwesenheitForm") ErfassungForm form,
                                       BindingResult bindingResult, Authentication authentication, Model model,
                                       RedirectAttributes redirectAttributes) {
        // Logge den Start der Verarbeitung mit den übergebenen Form-Daten.
        logger.debug("speichernAnwesenheit() wurde aufgerufen mit Datum {} und Form-Daten: {}", datum, form);
        zugriffService.pruefeGruppe(gruppeId, authentication);
        LocalDate tag = datum != null ? datum : LocalDate.now();

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
            return showFormAgain(gruppeId, tag, form, model);
        }

        try {
            // Delegiere die Prüfung und Speicherung der Anwesenheitsdaten an die Service-Schicht.
            erfassungService.erfassenAnwesenheiten(gruppeId, tag, form.getEintraege());

            // Logge den erfolgreichen Abschluss der Verarbeitung.
            logger.info("Anwesenheit für Gruppe {} am {} wurde erfolgreich verarbeitet.", gruppeId, tag);

            // Erfolgsmeldung für den Benutzer
            redirectAttributes.addFlashAttribute("successMessage",
                "Die Anwesenheit für den " + tag.format(DATUM_FORMAT) + " wurde gespeichert.");

            return tag.equals(LocalDate.now())
                    ? "redirect:/anwesenheit/" + gruppeId
                    : "redirect:/anwesenheit/" + gruppeId + "?datum=" + tag;

        } catch (IllegalArgumentException e) {
            // Ungültige Einträge (z. B. Student aus einer anderen Gruppe): es wurde nichts gespeichert.
            logger.warn("Ungültige Anwesenheitsdaten für Gruppe {}: {}", gruppeId, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("errorType", "validation");
            return showFormAgain(gruppeId, tag, form, model);
        } catch (Exception e) {
            logger.error("Fehler beim Speichern der Anwesenheitsdaten: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Fehler beim Speichern der Daten. Bitte versuchen Sie es erneut.");
            model.addAttribute("errorType", "database");
            return showFormAgain(gruppeId, tag, form, model);
        }
    }
}
