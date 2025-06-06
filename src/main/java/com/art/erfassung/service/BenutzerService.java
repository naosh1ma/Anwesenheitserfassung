package com.art.erfassung.service;

import com.art.erfassung.repository.BenutzerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Serviceklasse zur Verwaltung von Benutzern.
 * <p>
 * Diese Klasse stellt Methoden zur Authentifizierung von Benutzern zur Verfügung.
 * Dabei wird das {@link BenutzerRepository} genutzt, um Benutzerdaten abzurufen und
 * zu überprüfen, ob die eingegebenen Login-Daten korrekt sind.
 */
@Service
public class BenutzerService {

    // Repository zur Verwaltung der Benutzerdaten
    private final BenutzerRepository benutzerRepository;

    @Autowired
    public BenutzerService(BenutzerRepository benutzerRepository) {
        this.benutzerRepository = benutzerRepository;
    }

    /**
     * Überprüft die Login-Daten eines Benutzers.
     * <p>
     * Es wird versucht, einen Benutzer anhand des angegebenen Benutzernamens zu finden.
     * Falls ein Benutzer gefunden wird, wird das gespeicherte Passwort mit dem eingegebenen
     * Passwort verglichen.
     *
     * @param benutzername der Benutzername des anzumeldenden Benutzers
     * @param passwort     das Passwort des anzumeldenden Benutzers
     * @return {@code true}, wenn ein Benutzer gefunden wird und das Passwort übereinstimmt,
     *         andernfalls {@code false}
     */
    public boolean checkLogin(String benutzername, String passwort) {
        // Suche den Benutzer anhand des Benutzernamens und überprüfe das Passwort
        return benutzerRepository.findByBenutzername(benutzername)
                .map(benutzer -> benutzer.getPasswort().equals(passwort))
                .orElse(false);
    }
}