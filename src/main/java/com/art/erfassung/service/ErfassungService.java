package com.art.erfassung.service;

import com.art.erfassung.dto.AnwesenheitsDTO;
import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.StatusRepository;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    // Repository zur Verwaltung der Studenten-Daten
    private final StudentenRepository studentenRepository;

    private final StudentenService studentenService;
    private final StatusService statusService;

    @Autowired
    public ErfassungService(ErfassungRepository erfassungRepository, StatusRepository statusRepository, StudentenRepository studentenRepository, StudentenService studentenService, StatusService statusService) {
        this.erfassungRepository = erfassungRepository;
        this.statusRepository = statusRepository;
        this.studentenRepository = studentenRepository;
        this.studentenService = studentenService;
        this.statusService = statusService;
    }


    /**
     * Verarbeitet die Anwesenheitsdaten aus einer Liste von AnwesenheitsDTOs.
     * Berechnet die Verspätung, falls vorhanden, und speichert die Erfassungen.
     *
     * @param dtos eine Liste der Anwesenheitsdaten als DTOs
     * @return die Gruppen-ID, zu der die Erfassung gehört
     */
    public Integer processAnwesenheiten(List<AnwesenheitsDTO> dtos) {
        LocalDate datum = LocalDate.now();
        LocalTime expectedTime = LocalTime.of(8, 0);
        Integer gruppeId = null;
        List<Erfassung> erfassungenToSave = new ArrayList<>();

        for (AnwesenheitsDTO dto : dtos) {
            // Hole Studenten und Status
            Studenten student = studentenService.findOrThrow(dto.getStudentenId());
            Status status = statusService.findOrThrow(dto.getStatusId());
            String kommentar = dto.getKommentar();
            gruppeId = student.getGruppe().getId(); // Annahme: Alle Einträge gehören zur selben Gruppe

            // Verspätung berechnen, wenn Ankunftszeit nach expectedTime liegt
            LocalTime ankunftszeit = LocalTime.parse(dto.getAnkunftszeit());
            if (ankunftszeit.isAfter(expectedTime)) {
                long delayMinutes = ChronoUnit.MINUTES.between(expectedTime, ankunftszeit);
                kommentar = (kommentar != null && !kommentar.isEmpty())
                        ? kommentar + " | Verspätung: " + delayMinutes + " Minuten"
                        : "Verspätung: " + delayMinutes + " Minuten";
            }

            final String finalKommentar = kommentar; // Final für den Lambda-Ausdruck
            Erfassung erfassung = findByStudentAndDate(student.getId(), datum)
                    .map(e -> {
                        e.setStatus(status);
                        e.setKommentar(finalKommentar);
                        return e;
                    })
                    .orElseGet(() -> new Erfassung(student, datum, status, finalKommentar));

            erfassungenToSave.add(erfassung);
        }

        saveAll(erfassungenToSave);
        return gruppeId;
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
     * Speichert alle übergebenen Erfassungen in der Datenbank.
     * <p>
     * Diese Methode nutzt die Batch-Funktionalität von {@link ErfassungRepository}
     * um mehrere Erfassungen in einem einzigen Aufruf zu persistieren. Dadurch werden
     * Datenbankzugriffe reduziert und die Performance verbessert, sofern Hibernate entsprechend konfiguriert ist.
     * </p>
     *
     * @param erfassungenToSave die Liste der {@link Erfassung} Objekte, die gespeichert werden sollen
     */
    public void saveAll(List<Erfassung> erfassungenToSave) {
        erfassungRepository.saveAll(erfassungenToSave);
    }
}
