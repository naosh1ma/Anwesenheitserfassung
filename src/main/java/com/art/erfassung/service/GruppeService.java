package com.art.erfassung.service;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Serviceklasse zur Verwaltung von Gruppen.
 * <p>
 * Diese Klasse stellt Methoden zum Abrufen, Anlegen, Umbenennen und Löschen von Gruppen zur Verfügung.
 * Der Zugriff auf die Daten erfolgt über das {@link GruppeRepository}.
 */
@Service
public class GruppeService {

    // Sortierung nach Bezeichnung, unabhängig von Groß- und Kleinschreibung
    private static final Comparator<Gruppe> NACH_BEZEICHNUNG =
            Comparator.comparing(Gruppe::getBezeichnung, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    // Repository zur Verwaltung der Gruppen-Daten
    private final GruppeRepository gruppeRepository;
    // Repository zur Prüfung, ob eine Gruppe noch Studenten enthält
    private final StudentenRepository studentenRepository;

    @Autowired
    public GruppeService(GruppeRepository gruppeRepository, StudentenRepository studentenRepository) {
        this.gruppeRepository = gruppeRepository;
        this.studentenRepository = studentenRepository;
    }

    /**
     * Sucht eine Gruppe anhand ihrer ID.
     * <p>
     * Wird keine Gruppe mit der angegebenen ID gefunden, wird eine NoSuchElementException geworfen.
     *
     * @param id die ID der gesuchten Gruppe
     * @return die gefundene {@link Gruppe}
     * @throws NoSuchElementException wenn keine Gruppe mit der ID existiert
     */
    public Gruppe findOrThrow(Integer id) {
        // Suche der Gruppe und Fehler werfen, wenn sie nicht gefunden wird
        return gruppeRepository.findById(id).orElseThrow();
    }

    /**
     * Liefert eine Liste aller vorhandenen Gruppen, sortiert nach Bezeichnung.
     *
     * @return eine Liste aller {@link Gruppe} Objekte
     */
    public List<Gruppe> findAll() {
        // Abrufen aller Gruppen aus dem Repository
        return gruppeRepository.findAll().stream()
                .sorted(NACH_BEZEICHNUNG)
                .toList();
    }

    /**
     * Liefert die Gruppen, die einem Benutzer zugewiesen sind, sortiert nach Bezeichnung.
     *
     * @param benutzername der Benutzername
     * @return die zugewiesenen Gruppen
     */
    public List<Gruppe> findZugewiesene(String benutzername) {
        return gruppeRepository.findZugewieseneGruppen(benutzername).stream()
                .sorted(NACH_BEZEICHNUNG)
                .toList();
    }

    /**
     * Legt eine neue Gruppe an.
     *
     * @param bezeichnung die Bezeichnung der Gruppe (nicht leer, eindeutig)
     * @return die gespeicherte {@link Gruppe}
     * @throws IllegalArgumentException wenn die Bezeichnung leer oder bereits vergeben ist
     */
    @Transactional
    public Gruppe anlegen(String bezeichnung) {
        return gruppeRepository.save(new Gruppe(pruefeBezeichnung(bezeichnung, null)));
    }

    /**
     * Benennt eine Gruppe um.
     *
     * @param id          die ID der Gruppe
     * @param bezeichnung die neue Bezeichnung (nicht leer, eindeutig)
     * @throws IllegalArgumentException wenn die Bezeichnung leer oder bereits vergeben ist
     * @throws NoSuchElementException   wenn keine Gruppe mit der ID existiert
     */
    @Transactional
    public void umbenennen(Integer id, String bezeichnung) {
        Gruppe gruppe = findOrThrow(id);
        gruppe.setBezeichnung(pruefeBezeichnung(bezeichnung, id));
        gruppeRepository.save(gruppe);
    }

    /**
     * Löscht eine Gruppe. Gruppen mit Studenten (auch deaktivierten) können nicht gelöscht werden.
     *
     * @param id die ID der Gruppe
     * @throws IllegalArgumentException wenn die Gruppe noch Studenten enthält
     * @throws NoSuchElementException   wenn keine Gruppe mit der ID existiert
     */
    @Transactional
    public void loeschen(Integer id) {
        Gruppe gruppe = findOrThrow(id);
        long anzahlStudenten = studentenRepository.countByGruppeId(id);
        if (anzahlStudenten > 0) {
            throw new IllegalArgumentException("Die Gruppe '" + gruppe.getBezeichnung() + "' enthält noch Studenten ("
                    + anzahlStudenten + ", auch deaktivierte zählen). Verschieben oder löschen Sie diese zuerst.");
        }
        gruppeRepository.delete(gruppe);
    }

    private String pruefeBezeichnung(String bezeichnung, Integer eigeneId) {
        String name = bezeichnung == null ? "" : bezeichnung.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Bitte geben Sie eine Bezeichnung für die Gruppe ein.");
        }
        boolean vergeben = gruppeRepository.findAllByBezeichnungIgnoreCase(name).stream()
                .anyMatch(gruppe -> !gruppe.getId().equals(eigeneId));
        if (vergeben) {
            throw new IllegalArgumentException("Eine Gruppe mit der Bezeichnung '" + name + "' gibt es bereits.");
        }
        return name;
    }
}
