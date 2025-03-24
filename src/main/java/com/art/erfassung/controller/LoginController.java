package com.art.erfassung.controller;

import com.art.erfassung.dao.BenutzerDao;
import com.art.erfassung.dao.GruppeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final BenutzerDao benutzerDAO;

    @Autowired
    public LoginController(BenutzerDao benutzerDAO, GruppeDao gruppeDAO) {
        this.benutzerDAO = benutzerDAO;
    }

    @GetMapping  // Wenn ein Benutzer "/login" aufruft → Diese Methode wird aufgerufen
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Inkorrekte Login-Daten");
        }
        return "login"; // Zeigt die login.html Seite an
    }

    @PostMapping() // Diese Methode wird aufgerufen, wenn das Formular abgeschickt wird
    public String login(@RequestParam String benutzername, @RequestParam String passwort, RedirectAttributes redirectAttributes) {
        System.out.println("Login-Versuch für Benutzer: " + benutzername);

        if (benutzerDAO.checkLogin(benutzername, passwort)) {
            System.out.println("✅ Login erfolgreich → Weiterleitung zum Dashboard");
            return "willkommen"; // Erfolgreich → Weiterleiten
        } else {
            System.out.println("❌ Fehler: Falsche Login-Daten");
            redirectAttributes.addFlashAttribute("error", "Inkorrekte Login-Daten");
            return "login"; // Fehlgeschlagen → Zurück mit Fehler
        }
    }

}

