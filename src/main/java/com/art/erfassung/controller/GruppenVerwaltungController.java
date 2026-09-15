package com.art.erfassung.controller;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StudentenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller für die Verwaltung von Gruppen und Studenten.
 * <p>
 * Nur Administratoren haben Zugriff (siehe SecurityConfig). Gruppen können angelegt, umbenannt und,
 * sofern sie leer sind, gelöscht werden. Studenten können angelegt, bearbeitet, in eine andere Gruppe
 * verschoben, deaktiviert, reaktiviert und, solange sie keine Anwesenheitsdaten haben, gelöscht werden.
 * </p>
 */
@Controller
@RequestMapping("/admin/gruppen")
public class GruppenVerwaltungController {

    // Service zur Verwaltung der Gruppen
    private final GruppeService gruppeService;
    // Service zur Verwaltung der Studenten
    private final StudentenService studentenService;

    public GruppenVerwaltungController(GruppeService gruppeService, StudentenService studentenService) {
        this.gruppeService = gruppeService;
        this.studentenService = studentenService;
    }

    /**
     * Zeigt alle Gruppen mit der Anzahl ihrer aktiven und deaktivierten Studenten an.
     *
     * @param model das Model für die View
     * @return den Namen der View "admin-gruppen"
     */
    @GetMapping
    public String gruppenListe(Model model) {
        List<Gruppe> gruppen = gruppeService.findAll();
        Map<Integer, Long> aktiveStudenten = new HashMap<>();
        Map<Integer, Long> deaktivierteStudenten = new HashMap<>();
        for (Gruppe gruppe : gruppen) {
            List<Studenten> studenten = studentenService.findByGruppeId(gruppe.getId());
            long aktiv = studenten.stream().filter(Studenten::isAktiv).count();
            aktiveStudenten.put(gruppe.getId(), aktiv);
            deaktivierteStudenten.put(gruppe.getId(), studenten.size() - aktiv);
        }
        model.addAttribute("gruppen", gruppen);
        model.addAttribute("aktiveStudenten", aktiveStudenten);
        model.addAttribute("deaktivierteStudenten", deaktivierteStudenten);
        model.addAttribute("pageTitle", "Gruppen verwalten");
        return "admin-gruppen";
    }

    /**
     * Legt eine neue Gruppe an.
     */
    @PostMapping
    public String gruppeAnlegen(@RequestParam String bezeichnung, RedirectAttributes redirectAttributes) {
        try {
            Gruppe gruppe = gruppeService.anlegen(bezeichnung);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Die Gruppe '" + gruppe.getBezeichnung() + "' wurde angelegt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gruppen";
    }

    /**
     * Benennt eine Gruppe um.
     */
    @PostMapping("/{id}/umbenennen")
    public String gruppeUmbenennen(@PathVariable Integer id, @RequestParam String bezeichnung,
                                   RedirectAttributes redirectAttributes) {
        try {
            gruppeService.umbenennen(id, bezeichnung);
            redirectAttributes.addFlashAttribute("successMessage", "Die Gruppe wurde umbenannt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gruppen";
    }

    /**
     * Löscht eine leere Gruppe.
     */
    @PostMapping("/{id}/loeschen")
    public String gruppeLoeschen(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            gruppeService.loeschen(id);
            redirectAttributes.addFlashAttribute("successMessage", "Die Gruppe wurde gelöscht.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gruppen";
    }

    /**
     * Zeigt die Studenten einer Gruppe zur Verwaltung an: zuerst die aktiven, dann die deaktivierten.
     *
     * @param id    die ID der Gruppe
     * @param model das Model für die View
     * @return den Namen der View "admin-studenten"
     */
    @GetMapping("/{id}")
    public String studentenVerwalten(@PathVariable Integer id, Model model) {
        model.addAttribute("gruppe", gruppeService.findOrThrow(id));
        model.addAttribute("studenten", studentenService.findAlleByGruppeIdSortiert(id));
        model.addAttribute("alleGruppen", gruppeService.findAll());
        model.addAttribute("pageTitle", "Studenten verwalten");
        return "admin-studenten";
    }

    /**
     * Fügt einer Gruppe einen neuen Studenten hinzu.
     */
    @PostMapping("/{id}/studenten")
    public String studentAnlegen(@PathVariable Integer id, @RequestParam String vorname, @RequestParam String name,
                                 RedirectAttributes redirectAttributes) {
        try {
            Studenten student = studentenService.anlegen(id, vorname, name);
            redirectAttributes.addFlashAttribute("successMessage",
                    student.getVorname() + " " + student.getName() + " wurde hinzugefügt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gruppen/" + id;
    }

    /**
     * Speichert Vor- und Nachname eines Studenten und verschiebt ihn bei Bedarf in eine andere Gruppe.
     */
    @PostMapping("/{id}/studenten/{studentId}")
    public String studentBearbeiten(@PathVariable Integer id, @PathVariable Integer studentId,
                                    @RequestParam String vorname, @RequestParam String name,
                                    @RequestParam Integer gruppeId, RedirectAttributes redirectAttributes) {
        try {
            Studenten student = studentenService.bearbeiten(studentId, vorname, name, gruppeId);
            String meldung = student.getGruppe().getId().equals(id)
                    ? "Die Änderungen an " + student.getVorname() + " " + student.getName() + " wurden gespeichert."
                    : student.getVorname() + " " + student.getName() + " wurde in die Gruppe '"
                            + student.getGruppe().getBezeichnung() + "' verschoben.";
            redirectAttributes.addFlashAttribute("successMessage", meldung);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gruppen/" + id;
    }

    /**
     * Deaktiviert einen Studenten. Seine Anwesenheitsdaten bleiben erhalten.
     */
    @PostMapping("/{id}/studenten/{studentId}/deaktivieren")
    public String studentDeaktivieren(@PathVariable Integer id, @PathVariable Integer studentId,
                                      RedirectAttributes redirectAttributes) {
        Studenten student = studentenService.deaktivieren(studentId);
        redirectAttributes.addFlashAttribute("successMessage", student.getVorname() + " " + student.getName()
                + " wurde deaktiviert. Die bisherigen Anwesenheitsdaten bleiben erhalten.");
        return "redirect:/admin/gruppen/" + id;
    }

    /**
     * Reaktiviert einen deaktivierten Studenten.
     */
    @PostMapping("/{id}/studenten/{studentId}/reaktivieren")
    public String studentReaktivieren(@PathVariable Integer id, @PathVariable Integer studentId,
                                      RedirectAttributes redirectAttributes) {
        Studenten student = studentenService.reaktivieren(studentId);
        redirectAttributes.addFlashAttribute("successMessage",
                student.getVorname() + " " + student.getName() + " wurde reaktiviert.");
        return "redirect:/admin/gruppen/" + id;
    }

    /**
     * Löscht einen Studenten, sofern er noch keine Anwesenheitsdaten hat.
     */
    @PostMapping("/{id}/studenten/{studentId}/loeschen")
    public String studentLoeschen(@PathVariable Integer id, @PathVariable Integer studentId,
                                  RedirectAttributes redirectAttributes) {
        try {
            Studenten student = studentenService.findOrThrow(studentId);
            studentenService.loeschen(studentId);
            redirectAttributes.addFlashAttribute("successMessage",
                    student.getVorname() + " " + student.getName() + " wurde gelöscht.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gruppen/" + id;
    }
}
