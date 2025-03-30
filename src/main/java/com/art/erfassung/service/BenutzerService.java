package com.art.erfassung.service;

import com.art.erfassung.repository.BenutzerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BenutzerService {

    private final BenutzerRepository benutzerRepository;

    @Autowired
    public BenutzerService(BenutzerRepository benutzerRepository) {
        this.benutzerRepository = benutzerRepository;
    }

    public boolean checkLogin(String benutzername, String passwort) {
        return benutzerRepository.findByBenutzername(benutzername)                 // Suche Benutzer in DB
                .map(benutzer -> benutzer.getPasswort().equals(passwort)) // Falls Benutzer gefunden → Passwort prüfen
                .orElse(false);                                              // Falls Benutzer nicht existiert → false zurückgeben
    }
}

