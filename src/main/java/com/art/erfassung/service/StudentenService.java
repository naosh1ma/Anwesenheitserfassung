package com.art.erfassung.service;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Serviceklasse zur Verwaltung von Studenten.
 * <p>
 * Diese Klasse stellt Methoden zur Abfrage von Studenten bereit sowie zum Anlegen, Bearbeiten,
 * Verschieben, Deaktivieren, Reaktivieren und Löschen. Für den Datenzugriff wird das
 * {@link StudentenRepository} verwendet.
 */
@Service
public class StudentenService {

    // Sortierung nach Nachname, dann Vorname
    private static final Comparator<Studenten> NACH_NAME = Comparator
            .comparing(Studenten::getName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Studenten::getVorname, String.CASE_INSENSITIVE_ORDER);

    // Repository zur Verwaltung der Studenten-Daten
    private final StudentenRepository studentenRepository;
    // Repository zum Laden der Zielgruppe beim Anlegen und Verschieben
    private final GruppeRepository gruppeRepository;
    // Repository zur Prüfung, ob ein Student bereits Anwesenheitsdaten hat
    private final ErfassungRepository erfassungRepository;

    @Autowired
    public StudentenService(StudentenRepository studentenRepository, GruppeRepository gruppeRepository,
                            ErfassungRepository erfassungRepository) {
        this.studentenRepository = studentenRepository;
        this.gruppeRepository = gruppeRepository;
        this.erfassungRepository = erfassungRepository;
    }

    /**
     * Liefert eine Liste von Studenten, die der angegebenen Gruppe zugeordnet sind, aktive und deaktivierte.
     *
     * @param gruppeId die ID der Gruppe, deren Studenten gesucht werden
     * @return eine Liste von {@link Studenten} Objekten, die der Gruppe angehören
     */
    public List<Studenten> findByGruppeId(Integer gruppeId) {
        // Abfrage der Studenten anhand der Gruppen-ID im Repository
        return studentenRepository.findByGruppeId(gruppeId);
    }

    /**
     * Liefert die aktiven Studenten einer Gruppe, sortiert nach Name.
     *
     * @param gruppeId die ID der Gruppe
     * @return die aktiven Studenten der Gruppe
     */
    public List<Studenten> findAktiveByGruppeId(Integer gruppeId) {
        return studentenRepository.findByGruppeIdAndDeaktiviertAmIsNull(gruppeId).stream()
                .sorted(NACH_NAME)
                .toList();
    }

    /**
     * Liefert alle Studenten einer Gruppe: zuerst die aktiven, dann die deaktivierten, jeweils nach Name sortiert.
     *
     * @param gruppeId die ID der Gruppe
     * @return alle Studenten der Gruppe
     */
    public List<Studenten> findAlleByGruppeIdSortiert(Integer gruppeId) {
        return studentenRepository.findByGruppeId(gruppeId).stream()
                .sorted(Comparator.comparing((Studenten student) -> !student.isAktiv()).thenComparing(NACH_NAME))
                .toList();
    }

    /**
     * Sucht einen Studenten anhand seiner ID.
     * <p>
     * Falls kein Student mit der angegebenen ID gefunden wird, wird eine NoSuchElementException ausgelöst.
     *
     * @param id die ID des gesuchten Studenten
     * @return das gefundene {@link Studenten} Objekt
     * @throws NoSuchElementException wenn kein Student mit der ID existiert
     */
    public Studenten findOrThrow(Integer id) {
        // Suche des Studenten anhand der ID und Werfen einer Exception, falls nicht gefunden
        return studentenRepository.findById(id).orElseThrow();
    }

    /**
     * Legt einen neuen, aktiven Studenten in einer Gruppe an.
     *
     * @param gruppeId die ID der Gruppe
     * @param vorname  der Vorname
     * @param name     der Nachname
     * @return der gespeicherte Student
     * @throws IllegalArgumentException wenn Vor- oder Nachname leer ist
     * @throws NoSuchElementException   wenn die Gruppe nicht existiert
     */
    @Transactional
    public Studenten anlegen(Integer gruppeId, String vorname, String name) {
        String gepruefterVorname = pflichtfeld(vorname, "Bitte geben Sie einen Vornamen ein.");
        String gepruefterName = pflichtfeld(name, "Bitte geben Sie einen Nachnamen ein.");
        Gruppe gruppe = gruppeRepository.findById(gruppeId).orElseThrow();
        return studentenRepository.save(new Studenten(gepruefterName, gepruefterVorname, gruppe));
    }

    /**
     * Ändert Vor- und Nachname eines Studenten und verschiebt ihn bei Bedarf in eine andere Gruppe.
     * Bereits erfasste Anwesenheitsdaten gehören weiterhin zum Studenten und erscheinen danach in der neuen Gruppe.
     *
     * @param id       die ID des Studenten
     * @param vorname  der neue Vorname
     * @param name     der neue Nachname
     * @param gruppeId die ID der (neuen) Gruppe
     * @return der gespeicherte Student
     * @throws IllegalArgumentException wenn Vor- oder Nachname leer ist
     * @throws NoSuchElementException   wenn Student oder Gruppe nicht existieren
     */
    @Transactional
    public Studenten bearbeiten(Integer id, String vorname, String name, Integer gruppeId) {
        String gepruefterVorname = pflichtfeld(vorname, "Bitte geben Sie einen Vornamen ein.");
        String gepruefterName = pflichtfeld(name, "Bitte geben Sie einen Nachnamen ein.");
        Studenten student = findOrThrow(id);
        student.setVorname(gepruefterVorname);
        student.setName(gepruefterName);
        student.setGruppe(gruppeRepository.findById(gruppeId).orElseThrow());
        return studentenRepository.save(student);
    }

    /**
     * Deaktiviert einen Studenten ab heute. Er erscheint danach nicht mehr im Erfassungsformular,
     * seine Anwesenheitsdaten bleiben erhalten.
     *
     * @param id die ID des Studenten
     * @return der Student
     * @throws NoSuchElementException wenn kein Student mit der ID existiert
     */
    @Transactional
    public Studenten deaktivieren(Integer id) {
        Studenten student = findOrThrow(id);
        if (student.isAktiv()) {
            student.setDeaktiviertAm(LocalDate.now());
            studentenRepository.save(student);
        }
        return student;
    }

    /**
     * Reaktiviert einen deaktivierten Studenten.
     *
     * @param id die ID des Studenten
     * @return der Student
     * @throws NoSuchElementException wenn kein Student mit der ID existiert
     */
    @Transactional
    public Studenten reaktivieren(Integer id) {
        Studenten student = findOrThrow(id);
        student.setDeaktiviertAm(null);
        return studentenRepository.save(student);
    }

    /**
     * Löscht einen Studenten. Studenten mit Anwesenheitsdaten können nicht gelöscht werden,
     * damit keine erfassten Daten verloren gehen; sie können stattdessen deaktiviert werden.
     *
     * @param id die ID des Studenten
     * @throws IllegalArgumentException wenn der Student bereits Anwesenheitsdaten hat
     * @throws NoSuchElementException   wenn kein Student mit der ID existiert
     */
    @Transactional
    public void loeschen(Integer id) {
        Studenten student = findOrThrow(id);
        if (!erfassungRepository.findByStudenten_id(id).isEmpty()) {
            throw new IllegalArgumentException(student.getVorname() + " " + student.getName()
                    + " hat bereits Anwesenheitsdaten und kann nicht gelöscht werden. Deaktivieren Sie den Studenten stattdessen.");
        }
        studentenRepository.delete(student);
    }

    private static String pflichtfeld(String wert, String fehlermeldung) {
        if (wert == null || wert.isBlank()) {
            throw new IllegalArgumentException(fehlermeldung);
        }
        return wert.trim();
    }
}
