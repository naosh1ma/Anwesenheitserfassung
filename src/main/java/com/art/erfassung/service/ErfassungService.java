package com.art.erfassung.service;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Status;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.StatusRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Serviceklasse zur Verwaltung von Erfassungen.
 * <p>
 * Diese Klasse stellt Methoden zum Abrufen, Aktualisieren und Speichern von Erfassungen
 * bereit. Dabei wird sowohl das {@link ErfassungRepository} für Erfassungsdaten als auch
 * das {@link StatusRepository} für Statusdaten verwendet.
 */
@Service
public class ErfassungService {

    // Repository zur Verwaltung der Erfassungen
    private final ErfassungRepository erfassungRepository;
    // Repository zur Verwaltung der Statusinformationen
    private final StatusRepository statusRepository;

    /**
     * Konstruktor zur Initialisierung des ErfassungService mit den erforderlichen Repositories.
     *
     * @param erfassungRepository das Repository, das für die Erfassung von Daten zuständig ist.
     * @param statusRepository    das Repository, das Statusdaten verwaltet.
     */
    public ErfassungService(ErfassungRepository erfassungRepository, StatusRepository statusRepository) {
        this.erfassungRepository = erfassungRepository;
        this.statusRepository = statusRepository;
    }

    /**
     * Gibt eine Liste von Erfassungen zurück, die einer bestimmten Gruppe in einem bestimmten
     * Zeitraum zugeordnet sind.
     * <p>
     * Es werden alle Erfassungen für die Gruppe mit der angegebenen ID abgerufen, deren Datum
     * zwischen dem Start- und Enddatum liegt.
     *
     * @param gruppeId   die ID der Gruppe, für die die Erfassungen gesucht werden.
     * @param monatStart das Startdatum des Monats.
     * @param monatEnde  das Enddatum des Monats.
     * @return eine Liste von {@link Erfassung} Objekten, die den Kriterien entsprechen.
     */
    public List<Erfassung> getByGruppeUndMonat(Integer gruppeId, LocalDate monatStart, LocalDate monatEnde) {
        // Abrufen aller Erfassungen für die angegebene Gruppe und den Zeitraum
        return erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(gruppeId, monatStart, monatEnde);
    }

    /**
     * Aktualisiert eine bestehende Erfassung mit einem neuen Status und Kommentar.
     * <p>
     * Es wird die Erfassung anhand der übergebenen ID gesucht. Falls diese vorhanden ist, wird
     * der Status anhand der angegebenen Status-ID aktualisiert und der Kommentar gesetzt. Anschließend
     * wird die Erfassung gespeichert.
     *
     * @param erfassungId die ID der zu aktualisierenden Erfassung.
     * @param statusId    die ID des neuen Status, der gesetzt werden soll.
     * @param kommentar der Kommentar, der der Erfassung hinzugefügt werden soll.
     */
    public void updateErfassung(Integer erfassungId, Integer statusId, String kommentar) {
        // Suchen der Erfassung anhand der ID
        Optional<Erfassung> optionalErfassung = erfassungRepository.findById(erfassungId);
        // Aktualisieren des Status und Kommentars in der Erfassung
        optionalErfassung.ifPresent(erfassung -> {
            Status neuerStatus = statusRepository.findById(statusId).orElseThrow();
            erfassung.setStatus(neuerStatus);
            erfassung.setKommentar(kommentar);
            // Speichern der aktualisierten Erfassung
            erfassungRepository.save(erfassung);
        });
    }

    /**
     * Speichert eine neue oder aktualisierte Erfassung.
     *
     * @param erfassung das {@link Erfassung} Objekt, das gespeichert werden soll.
     */
    public void save(Erfassung erfassung) {
        // Speichern der Erfassung im Repository
        erfassungRepository.save(erfassung);
    }

    /**
     * Sucht eine Erfassung anhand der Studenten-ID und eines bestimmten Datums.
     *
     * @param studentId die ID des Studenten, dessen Erfassung gesucht wird.
     * @param date      das Datum, für das die Erfassung gesucht wird.
     * @return ein {@link Optional} mit dem gefundenen {@link Erfassung} Objekt,
     *         oder leer, falls keine Erfassung gefunden wird.
     */
    public Optional<Erfassung> findByStudentAndDate(Integer studentId, LocalDate date) {
        // Abrufen der Erfassung anhand der Studenten-ID und des Datums
        return erfassungRepository.findByStudenten_IdAndDatum(studentId, date);
    }

    /**
     * Sucht eine Erfassung anhand der Erfassungs-ID.
     *
     * @param id die ID der Erfassung.
     * @return ein {@link Optional} mit dem gefundenen {@link Erfassung} Objekt,
     *         oder leer, falls keine Erfassung gefunden wird.
     */
    public Optional<Erfassung> findById(Integer id) {
        // Abrufen der Erfassung anhand der ID
        return erfassungRepository.findById(id);
    }
}
