package com.art.erfassung.controller;

import com.art.erfassung.dao.BenutzerDao;
import com.art.erfassung.dao.GruppeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
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
        if (benutzerDAO.checkLogin(benutzername, passwort)) {
            return "willkommen"; // Erfolgreich → Weiterleiten
        } else {
            return "login"; // Fehlgeschlagen → Zurück mit Fehler
        }
    }


}

