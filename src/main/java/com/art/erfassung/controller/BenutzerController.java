package com.art.erfassung.controller;

import com.art.erfassung.dto.BenutzerForm;
import com.art.erfassung.model.Benutzer;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Rolle;
import com.art.erfassung.service.BenutzerService;
import com.art.erfassung.service.GruppeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller für die Benutzerverwaltung.
 * <p>
 * Nur Administratoren haben Zugriff (siehe SecurityConfig). Sie können Benutzer anlegen,
 * Lehrern Gruppen zuweisen, Passwörter neu setzen und Benutzer löschen.
 * </p>
 */
@Controller
@RequestMapping("/admin/benutzer")
public class BenutzerController {

    // Service zur Verwaltung der Benutzer
    private final BenutzerService benutzerService;
    // Service zum Laden der Gruppen, die Lehrern zugewiesen werden können
    private final GruppeService gruppeService;

    public BenutzerController(BenutzerService benutzerService, GruppeService gruppeService) {
        this.benutzerService = benutzerService;
        this.gruppeService = gruppeService;
    }

    /**
     * Zeigt die Liste aller Benutzer und das Formular zum Anlegen eines Benutzers an.
     *
     * @param model das Model für die View
     * @return den Namen der View "benutzer"
     */
    @GetMapping
    public String benutzerListe(Model model) {
        model.addAttribute("benutzerForm", new BenutzerForm());
        return zeigeSeite(model);
    }

    /**
     * Legt einen neuen Benutzer an.
     *
     * @param form               die Formulardaten
     * @param bindingResult      das Ergebnis der Validierung
     * @param model              das Model für die View
     * @param redirectAttributes Attribute für die Erfolgsmeldung nach dem Redirect
     * @return Redirect auf die Benutzerliste oder bei Fehlern der View "benutzer"
     */
    @PostMapping
    public String benutzerAnlegen(@Valid @ModelAttribute("benutzerForm") BenutzerForm form, BindingResult bindingResult,
                                  Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<String> errorDetails = bindingResult.getAllErrors().stream()
                    .map(error -> error instanceof FieldError fieldError && fieldError.isBindingFailure()
                            ? "Ungültiger Wert für " + fieldError.getField()
                            : error.getDefaultMessage())
                    .distinct()
                    .toList();
            model.addAttribute("errorMessage", "Bitte korrigieren Sie folgende Fehler:");
            model.addAttribute("errorDetails", errorDetails);
            return zeigeSeite(model);
        }
        try {
            Benutzer benutzer = benutzerService.anlegen(form.getBenutzername(), form.getVorname(), form.getName(),
                    form.getRolle(), form.getPasswort(), form.getGruppenIds());
            String hinweis = !benutzer.isAdmin() && benutzer.getGruppen().isEmpty()
                    ? " Weisen Sie noch Gruppen zu, damit der Lehrer Anwesenheit erfassen kann." : "";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Benutzer '" + benutzer.getBenutzername() + "' wurde angelegt." + hinweis);
            return "redirect:/admin/benutzer";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return zeigeSeite(model);
        }
    }

    /**
     * Zeigt an, welche Gruppen einem Benutzer zugewiesen sind, und erlaubt die Änderung.
     *
     * @param id    die ID des Benutzers
     * @param model das Model für die View
     * @return den Namen der View "admin-benutzer-gruppen"
     */
    @GetMapping("/{id}/gruppen")
    public String gruppenBearbeiten(@PathVariable Integer id, Model model) {
        Benutzer benutzer = benutzerService.findMitGruppen(id);
        model.addAttribute("benutzer", benutzer);
        model.addAttribute("alleGruppen", gruppeService.findAll());
        model.addAttribute("zugewieseneIds", benutzer.getGruppen().stream().map(Gruppe::getId).collect(Collectors.toSet()));
        return "admin-benutzer-gruppen";
    }

    /**
     * Speichert die Gruppen, die ein Lehrer sehen darf. Bisherige Zuweisungen werden ersetzt.
     *
     * @param id                 die ID des Benutzers
     * @param gruppenIds         die ausgewählten Gruppen; ohne Auswahl werden alle Zuweisungen entfernt
     * @param redirectAttributes Attribute für die Meldung nach dem Redirect
     * @return Redirect auf die Benutzerliste oder bei Fehlern zurück auf die Zuweisung
     */
    @PostMapping("/{id}/gruppen")
    public String gruppenSpeichern(@PathVariable Integer id, @RequestParam(required = false) List<Integer> gruppenIds,
                                   RedirectAttributes redirectAttributes) {
        try {
            Benutzer benutzer = benutzerService.gruppenZuweisen(id, gruppenIds == null ? List.of() : gruppenIds);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Die Gruppen von '" + benutzer.getBenutzername() + "' wurden gespeichert.");
            return "redirect:/admin/benutzer";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/benutzer/" + id + "/gruppen";
        }
    }

    /**
     * Setzt ein neues Passwort für einen Benutzer.
     *
     * @param id                 die ID des Benutzers
     * @param passwort           das neue Passwort
     * @param redirectAttributes Attribute für die Meldung nach dem Redirect
     * @return Redirect auf die Benutzerliste
     */
    @PostMapping("/{id}/passwort")
    public String passwortSetzen(@PathVariable Integer id, @RequestParam String passwort,
                                 RedirectAttributes redirectAttributes) {
        try {
            benutzerService.passwortSetzen(id, passwort);
            redirectAttributes.addFlashAttribute("successMessage", "Das Passwort wurde geändert.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/benutzer";
    }

    /**
     * Löscht einen Benutzer. Das eigene Konto kann nicht gelöscht werden.
     *
     * @param id                 die ID des Benutzers
     * @param principal          der angemeldete Benutzer
     * @param redirectAttributes Attribute für die Meldung nach dem Redirect
     * @return Redirect auf die Benutzerliste
     */
    @PostMapping("/{id}/loeschen")
    public String benutzerLoeschen(@PathVariable Integer id, Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            benutzerService.loeschen(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Der Benutzer wurde gelöscht.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/benutzer";
    }

    private String zeigeSeite(Model model) {
        model.addAttribute("benutzerListe", benutzerService.findAll());
        model.addAttribute("rollen", Rolle.values());
        model.addAttribute("alleGruppen", gruppeService.findAll());
        return "benutzer";
    }
}
