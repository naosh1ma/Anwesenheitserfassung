package com.art.erfassung.controller;

import com.art.erfassung.service.BenutzerService;
import com.art.erfassung.service.GruppeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller zur Verwaltung des Login-Prozesses.
 * <p>
 * Dieser Controller verarbeitet Anfragen für den Login-Bereich der Anwendung.
 * Er zeigt die Login-Seite an und verarbeitet die Login-Formulareingaben.
 * </p>
 */
@Controller
@RequestMapping("/")
public class LoginController {

    // Service zur Authentifizierung von Benutzern
    private final BenutzerService benutzerService;

    @Autowired
    public LoginController(BenutzerService benutzerService, GruppeService gruppeService) {
        this.benutzerService = benutzerService;
    }

    /**
     * Zeigt die Login-Seite an.
     * <p>
     * Diese Methode verarbeitet GET-Anfragen. Falls ein Fehlerparameter übergeben wird,
     * wird eine entsprechende Fehlermeldung im Model angezeigt.
     * </p>
     *
     * @param error (optional) ein Fehlerparameter, der angibt, dass ein Fehler beim Login aufgetreten ist
     * @param model das Model, in dem die Daten für die View gespeichert werden
     * @return den Namen der View "login", die die Login-Seite darstellt
     */
    @GetMapping
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        // Falls ein Fehler übergeben wird, Fehlermeldung dem Model hinzufügen
        if (error != null) {
            model.addAttribute("error", "Inkorrekte Login-Daten");
        }
        // Rückgabe der View "login"
        return "login";
    }

    /**
     * Verarbeitet die Login-Anfrage.
     * <p>
     * Diese Methode verarbeitet POST-Anfragen, wenn das Login-Formular abgeschickt wird.
     * Es werden der Benutzername und das Passwort überprüft. Bei erfolgreicher Authentifizierung
     * wird der Benutzer zur Willkommensseite weitergeleitet, andernfalls verbleibt er auf der Login-Seite.
     * </p>
     *
     * @param benutzername der eingegebene Benutzername
     * @param passwort     das eingegebene Passwort
     * @return den Namen der View, "willkommen" bei Erfolg oder "login" bei Misserfolg
     */
    @PostMapping() // Diese Methode wird aufgerufen, wenn das Formular abgeschickt wird
    public String login(@RequestParam String benutzername, @RequestParam String passwort) {
        // Überprüfen der Login-Daten mittels des BenutzerService
        if (benutzerService.checkLogin(benutzername, passwort)) {
            // Erfolgreicher Login: Weiterleitung zur Willkommensseite
            return "willkommen";
        } else {
            // Fehlgeschlagener Login: Zurück zur Login-Seite
            return "login";
        }
    }


}

