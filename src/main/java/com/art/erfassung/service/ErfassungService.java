package com.art.erfassung.service;

import com.art.erfassung.dto.AnwesenheitDTO;
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
import java.time.format.DateTimeParseException;
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

    @Autowired
    public ErfassungService(ErfassungRepository erfassungRepository, StatusRepository statusRepository, StudentenRepository studentenRepository) {
        this.erfassungRepository = erfassungRepository;
        this.statusRepository = statusRepository;
        this.studentenRepository = studentenRepository;
    }

    /**
     * Verarbeitet eine Liste von AnwesenheitDTOs zur Erfassung der Anwesenheitsdaten.
     * <p>
     * Diese Methode führt folgende Aufgaben aus:
     * <ul>
     *   <li>Bestimmt das aktuelle Datum und definiert als Soll-Ankunftszeit 08:00 Uhr.</li>
     *   <li>Iteriert über die übergebene Liste von AnwesenheitDTOs:
     *     <ul>
     *       <li>Lädt für jeden DTO den entsprechenden Studenten und den Status mithilfe der entsprechenden Services.</li>
     *       <li>Setzt die Gruppen-ID anhand des Studenten – es wird angenommen, dass alle Einträge derselben Gruppe angehören.</li>
     *       <li>Wenn der Eingabestring für die Ankunftszeit leer oder null ist, wird angenommen, dass der Student pünktlich (08:00 Uhr) erschienen ist.</li>
     *       <li>Falls die tatsächliche Ankunftszeit später als die Soll-Ankunftszeit ist, wird die Verspätung in Minuten berechnet und in den Kommentar aufgenommen.</li>
     *       <li>Überprüft, ob bereits eine Erfassung für den Studenten am aktuellen Datum existiert.
     *           Falls ja, wird der bestehende Datensatz aktualisiert, ansonsten wird ein neuer Eintrag erstellt.</li>
     *     </ul>
     *   </li>
     *   <li>Alle Erfassungen werden in einer Liste gesammelt und als Batch gespeichert.</li>
     * </ul>
     * </p>
     *
     * @param dtos Eine Liste von AnwesenheitDTOs, die die vom Benutzer eingegebenen Anwesenheitsdaten enthalten.
     * @return Die Gruppen-ID, zu der die Anwesenheitsdatensätze gehören, oder null, falls sie nicht ermittelt werden konnte.
     * @throws DateTimeParseException falls ein nicht-leerer Ankunftszeit-String nicht in ein {@link LocalTime} geparst werden kann.
     */

    public Integer erfassenAnwesenheiten(List<AnwesenheitDTO> dtos) {
        LocalDate datum = LocalDate.now();
        LocalTime expectedTime = LocalTime.of(8, 0);
        Integer gruppeId = null;
        List<Erfassung> erfassungenToSave = new ArrayList<>();
        for (AnwesenheitDTO dto : dtos) {
            Studenten student = studentenRepository.findById(dto.getStudentenId()).orElseThrow();
            Status status = statusRepository.findById(dto.getStatusId()).orElseThrow();
            String kommentar = dto.getKommentar();
            gruppeId = student.getGruppe().getId();
            String ankunftStr = dto.getAnkunftszeit();
            LocalTime ankunftszeit;
            if (ankunftStr == null || ankunftStr.trim().isEmpty()) {
                ankunftszeit = expectedTime;
            } else {
                ankunftszeit = LocalTime.parse(ankunftStr);
            }
            if (ankunftszeit.isAfter(expectedTime)) {
                long delayMinutes = ChronoUnit.MINUTES.between(expectedTime, ankunftszeit);
                kommentar = (kommentar != null && !kommentar.isEmpty())
                        ? kommentar + " | Verspätung: " + delayMinutes + " Minuten"
                        : "Verspätung: " + delayMinutes + " Minuten";
            }
            final String finalKommentar = kommentar;
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
    public List<Erfassung> findByGruppeUndMonat(Integer gruppeId, LocalDate monatStart, LocalDate monatEnde) {
        // Abrufen aller Erfassungen für die angegebene Gruppe und den Zeitraum
        return erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(gruppeId, monatStart, monatEnde);
    }

    /**
     * Sucht eine Erfassung anhand der Studenten-ID und eines bestimmten Datums.
     *
     * @param studentId die ID des Studenten, dessen Erfassung gesucht wird.
     * @param date      das Datum, für das die Erfassung gesucht wird.
     * @return ein {@link Optional} mit dem gefundenen {@link Erfassung} Objekt,
     * oder leer, falls keine Erfassung gefunden wird.
     */
    public Optional<Erfassung> findByStudentAndDate(Integer studentId, LocalDate date) {
        // Abrufen der Erfassung anhand der Studenten-ID und des Datums
        return erfassungRepository.findByStudenten_IdAndDatum(studentId, date);
    }
}
