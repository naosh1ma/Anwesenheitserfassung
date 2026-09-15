package com.art.erfassung.service;

import com.art.erfassung.model.Benutzer;
import com.art.erfassung.model.Rolle;
import com.art.erfassung.repository.BenutzerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Serviceklasse zur Verwaltung von Benutzern.
 * <p>
 * Diese Klasse lädt Benutzer für die Anmeldung über Spring Security aus der Datenbank,
 * legt Benutzer an, setzt Passwörter und löscht Benutzer. Passwörter werden ausschließlich
 * als BCrypt-Hash gespeichert.
 * </p>
 */
@Service
public class BenutzerService implements UserDetailsService {

    // Mindestlänge für Passwörter
    public static final int MIN_PASSWORT_LAENGE = 8;

    // Format eines BCrypt-Hashes, z. B. "$2a$10$..." (entspricht der Prüfung in BCryptPasswordEncoder)
    private static final Pattern BCRYPT_HASH = Pattern.compile("\\A\\$2[aby]?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

    private static final Logger logger = LoggerFactory.getLogger(BenutzerService.class);

    // Repository zur Verwaltung der Benutzerdaten
    private final BenutzerRepository benutzerRepository;
    // Encoder zum Hashen und Prüfen von Passwörtern
    private final PasswordEncoder passwordEncoder;

    public BenutzerService(BenutzerRepository benutzerRepository, PasswordEncoder passwordEncoder) {
        this.benutzerRepository = benutzerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lädt einen Benutzer für die Anmeldung.
     * <p>
     * Benutzer ohne Passwort werden als deaktiviert zurückgegeben und können sich nicht anmelden.
     * Benutzer ohne Rolle erhalten die Rolle {@link Rolle#TEACHER}.
     * </p>
     *
     * @param benutzername der eingegebene Benutzername
     * @return die Anmeldedaten des Benutzers
     * @throws UsernameNotFoundException wenn kein Benutzer mit diesem Namen existiert
     */
    @Override
    public UserDetails loadUserByUsername(String benutzername) throws UsernameNotFoundException {
        Benutzer benutzer = benutzerRepository.findByBenutzername(benutzername)
                .orElseThrow(() -> new UsernameNotFoundException("Unbekannter Benutzer: " + benutzername));
        boolean ohnePasswort = benutzer.getPasswort() == null || benutzer.getPasswort().isEmpty();
        Rolle rolle = benutzer.getRolle() != null ? benutzer.getRolle() : Rolle.TEACHER;
        return User.withUsername(benutzer.getBenutzername())
                .password(ohnePasswort ? "" : benutzer.getPasswort())
                .roles(rolle.name())
                .disabled(ohnePasswort)
                .build();
    }

    /**
     * Gibt alle Benutzer sortiert nach Benutzername zurück.
     *
     * @return eine Liste aller {@link Benutzer}
     */
    public List<Benutzer> findAll() {
        return benutzerRepository.findAll(Sort.by("benutzername"));
    }

    /**
     * Legt einen neuen Benutzer mit gehashtem Passwort an.
     *
     * @param benutzername der Benutzername (muss eindeutig sein)
     * @param vorname      der Vorname
     * @param name         der Nachname
     * @param rolle        die Rolle des Benutzers
     * @param passwort     das Passwort im Klartext (mindestens {@value #MIN_PASSWORT_LAENGE} Zeichen)
     * @return der gespeicherte {@link Benutzer}
     * @throws IllegalArgumentException wenn der Benutzername vergeben oder das Passwort zu kurz ist
     */
    @Transactional
    public Benutzer anlegen(String benutzername, String vorname, String name, Rolle rolle, String passwort) {
        String login = benutzername.trim();
        if (benutzerRepository.findByBenutzername(login).isPresent()) {
            throw new IllegalArgumentException("Der Benutzername '" + login + "' ist bereits vergeben.");
        }
        pruefePasswort(passwort);
        Benutzer benutzer = new Benutzer(login, vorname.trim(), name.trim(), rolle);
        benutzer.setPasswort(passwordEncoder.encode(passwort));
        return benutzerRepository.save(benutzer);
    }

    /**
     * Setzt ein neues Passwort für einen Benutzer.
     *
     * @param id       die ID des Benutzers
     * @param passwort das neue Passwort im Klartext (mindestens {@value #MIN_PASSWORT_LAENGE} Zeichen)
     * @throws IllegalArgumentException wenn das Passwort zu kurz ist
     * @throws java.util.NoSuchElementException wenn kein Benutzer mit dieser ID existiert
     */
    @Transactional
    public void passwortSetzen(Integer id, String passwort) {
        pruefePasswort(passwort);
        Benutzer benutzer = benutzerRepository.findById(id).orElseThrow();
        benutzer.setPasswort(passwordEncoder.encode(passwort));
        benutzerRepository.save(benutzer);
    }

    /**
     * Löscht einen Benutzer. Das eigene Konto kann nicht gelöscht werden, damit immer
     * mindestens der handelnde Administrator erhalten bleibt.
     *
     * @param id                  die ID des zu löschenden Benutzers
     * @param aktuellerBenutzername der Benutzername des angemeldeten Benutzers
     * @throws IllegalArgumentException wenn das eigene Konto gelöscht werden soll
     * @throws java.util.NoSuchElementException wenn kein Benutzer mit dieser ID existiert
     */
    @Transactional
    public void loeschen(Integer id, String aktuellerBenutzername) {
        Benutzer benutzer = benutzerRepository.findById(id).orElseThrow();
        if (benutzer.getBenutzername().equals(aktuellerBenutzername)) {
            throw new IllegalArgumentException("Sie können Ihr eigenes Konto nicht löschen.");
        }
        benutzerRepository.delete(benutzer);
    }

    /**
     * Hasht Passwörter, die noch im Klartext gespeichert sind, und setzt fehlende Rollen auf {@link Rolle#TEACHER}.
     * <p>
     * Frühere Versionen der Anwendung haben Passwörter im Klartext verglichen. Bestehende Benutzer
     * behalten dadurch ihr Passwort, es liegt danach aber nur noch als BCrypt-Hash vor.
     * </p>
     *
     * @return die Anzahl der aktualisierten Benutzer
     */
    @Transactional
    public int migrierePasswoerter() {
        List<Benutzer> geaendert = new ArrayList<>();
        for (Benutzer benutzer : benutzerRepository.findAll()) {
            boolean aenderung = false;
            String passwort = benutzer.getPasswort();
            if (passwort != null && !passwort.isEmpty() && !BCRYPT_HASH.matcher(passwort).matches()) {
                benutzer.setPasswort(passwordEncoder.encode(passwort));
                aenderung = true;
            }
            if (benutzer.getRolle() == null) {
                benutzer.setRolle(Rolle.TEACHER);
                aenderung = true;
            }
            if (aenderung) {
                geaendert.add(benutzer);
            }
        }
        if (!geaendert.isEmpty()) {
            benutzerRepository.saveAll(geaendert);
            logger.info("{} bestehende Benutzer aktualisiert (Klartext-Passwort gehasht und/oder Rolle gesetzt).", geaendert.size());
        }
        return geaendert.size();
    }

    /**
     * Legt den ersten Administrator an, falls noch keiner existiert.
     * <p>
     * Existiert bereits ein Benutzer mit dem angegebenen Benutzernamen, wird er zum Administrator
     * gemacht und erhält das angegebene Passwort. Existiert schon ein Administrator, passiert nichts.
     * </p>
     *
     * @param benutzername der Benutzername aus ADMIN_USERNAME (darf leer sein)
     * @param passwort     das Passwort aus ADMIN_PASSWORD (darf leer sein)
     * @return {@code true}, wenn ein Administrator angelegt oder ernannt wurde
     */
    @Transactional
    public boolean erstelleAdminFallsNoetig(String benutzername, String passwort) {
        if (benutzerRepository.existsByRolle(Rolle.ADMIN)) {
            return false;
        }
        if (benutzername == null || benutzername.isBlank() || passwort == null || passwort.isBlank()) {
            logger.warn("Es gibt keinen Administrator. Setzen Sie ADMIN_USERNAME und ADMIN_PASSWORD, "
                    + "damit beim Start einer angelegt wird.");
            return false;
        }
        if (passwort.length() < MIN_PASSWORT_LAENGE) {
            logger.warn("ADMIN_PASSWORD muss mindestens {} Zeichen lang sein. Es wurde kein Administrator angelegt.",
                    MIN_PASSWORT_LAENGE);
            return false;
        }
        String login = benutzername.trim();
        Benutzer admin = benutzerRepository.findByBenutzername(login)
                .orElseGet(() -> new Benutzer(login, "", "Administrator", Rolle.ADMIN));
        admin.setRolle(Rolle.ADMIN);
        admin.setPasswort(passwordEncoder.encode(passwort));
        benutzerRepository.save(admin);
        logger.info("Administrator '{}' wurde angelegt.", login);
        return true;
    }

    private static void pruefePasswort(String passwort) {
        if (passwort == null || passwort.length() < MIN_PASSWORT_LAENGE) {
            throw new IllegalArgumentException(
                    "Das Passwort muss mindestens " + MIN_PASSWORT_LAENGE + " Zeichen lang sein.");
        }
    }
}
