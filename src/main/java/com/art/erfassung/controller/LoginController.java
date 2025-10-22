package com.art.erfassung.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller zur Verwaltung des Login-Prozesses.
 * <p>
 * Dieser Controller verarbeitet Anfragen für den Login-Bereich der Anwendung.
 * Er zeigt die Login-Seite an und verarbeitet Login-bezogene Anfragen.
 * Die eigentliche Authentifizierung wird von Spring Security übernommen.
 * </p>
 */
@Controller
@RequestMapping("/")
public class LoginController {

    /**
     * Zeigt die Login-Seite an.
     * <p>
     * Diese Methode verarbeitet GET-Anfragen für die Login-Seite.
     * Falls ein Fehlerparameter übergeben wird, wird eine entsprechende
     * Fehlermeldung im Model angezeigt.
     * </p>
     *
     * @param error (optional) ein Fehlerparameter, der angibt, dass ein Fehler beim Login aufgetreten ist
     * @param logout (optional) ein Parameter, der angibt, dass der Benutzer sich erfolgreich abgemeldet hat
     * @param model das Model, in dem die Daten für die View gespeichert werden
     * @return den Namen der View "login", die die Login-Seite darstellt
     */
    @GetMapping
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        // Falls ein Fehler übergeben wird, Fehlermeldung dem Model hinzufügen
        if (error != null) {
            model.addAttribute("error", "Ungültige Benutzername/Passwort-Kombination");
        }
        
        // Falls ein Logout-Parameter übergeben wird, Erfolgsmeldung dem Model hinzufügen
        if (logout != null) {
            model.addAttribute("message", "Sie haben sich erfolgreich abgemeldet");
        }
        
        // Rückgabe der View "login"
        return "login";
    }

    /**
     * Zeigt die Willkommensseite an.
     * <p>
     * Diese Methode zeigt die Hauptseite der Anwendung an, nachdem der Benutzer
     * sich erfolgreich angemeldet hat. Sie zeigt Informationen über den
     * angemeldeten Benutzer an.
     * </p>
     *
     * @param model das Model, in dem die Daten für die View gespeichert werden
     * @return den Namen der View "willkommen"
     */
    @GetMapping("/willkommen")
    public String willkommen(Model model) {
        // Informationen über den angemeldeten Benutzer abrufen
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        model.addAttribute("username", username);
        model.addAttribute("isAdmin", isAdmin);
        
        return "willkommen";
    }
}