package com.art.erfassung.service;

import com.art.erfassung.dto.ErfassungDTO;
import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.StatusRepository;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Serviceklasse zur Verwaltung von Erfassungen.
 * <p>
 * Diese Klasse stellt Methoden zum Abrufen, Aktualisieren und Speichern von Erfassungen
 * bereit. Dabei wird sowohl das {@link ErfassungRepository} für Erfassungsdaten als auch
 * das {@link StatusRepository} für Statusdaten verwendet.
 */
@Service
public class ErfassungService {

    // Uhrzeiten aus dem Formular, z. B. "08:30" (auch "8:30" wird akzeptiert)
    private static final DateTimeFormatter ZEIT_FORMAT = DateTimeFormatter.ofPattern("H:mm");

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
     * Speichert die Anwesenheit einer Gruppe für den heutigen Tag.
     * <p>
     * Diese Methode führt folgende Aufgaben aus:
     * <ul>
     *   <li>Lädt die Studenten der Gruppe, alle Status und die heutigen Erfassungen der Gruppe mit je einer Abfrage.</li>
     *   <li>Prüft alle Einträge: Jeder Student muss zur Gruppe gehören und aktiv sein, der Status muss existieren und
     *       die Verlassen-Zeit darf nicht vor der Ankunftszeit liegen. Ist ein Eintrag ungültig, wird nichts geändert.</li>
     *   <li>Aktualisiert eine bestehende Erfassung des Studenten für heute oder legt eine neue an.
     *       Ankunfts- und Verlassen-Zeit werden in eigenen Spalten gespeichert, leere Werte als {@code null}.</li>
     *   <li>Speichert alle Erfassungen in einer Transaktion.</li>
     * </ul>
     * </p>
     *
     * @param gruppeId die ID der Gruppe, für die die Anwesenheit erfasst wird
     * @param dtos     die vom Benutzer eingegebenen Anwesenheitsdaten
     * @throws IllegalArgumentException wenn ein Student nicht zur Gruppe gehört oder deaktiviert ist, ein Status
     *                                  unbekannt ist oder die Verlassen-Zeit vor der Ankunftszeit liegt
     * @throws java.time.format.DateTimeParseException wenn eine Uhrzeit nicht im Format HH:MM vorliegt
     */
    @Transactional
    public void erfassenAnwesenheiten(Integer gruppeId, List<ErfassungDTO> dtos) {
        LocalDate heute = LocalDate.now();
        Map<Integer, Studenten> studentenDerGruppe = studentenRepository.findByGruppeId(gruppeId).stream()
                .collect(Collectors.toMap(Studenten::getId, Function.identity()));
        Map<Integer, Status> statusNachId = statusRepository.findAll().stream()
                .collect(Collectors.toMap(Status::getId, Function.identity()));

        // Zuerst alle Einträge prüfen, damit bei einem ungültigen Eintrag keine Erfassung verändert wird
        List<GeprueftEintrag> eintraege = new ArrayList<>();
        for (ErfassungDTO dto : dtos) {
            Studenten student = studentenDerGruppe.get(dto.getStudentenId());
            if (student == null) {
                throw new IllegalArgumentException(
                        "Der Student mit der ID " + dto.getStudentenId() + " gehört nicht zu dieser Gruppe.");
            }
            if (!student.isAktiv()) {
                throw new IllegalArgumentException(student.getVorname() + " " + student.getName()
                        + " ist deaktiviert. Für deaktivierte Studenten kann keine Anwesenheit erfasst werden.");
            }
            Status status = statusNachId.get(dto.getStatusId());
            if (status == null) {
                throw new IllegalArgumentException("Unbekannter Status mit der ID " + dto.getStatusId() + ".");
            }
            LocalTime ankunftszeit = parseZeit(dto.getAnkunftszeit());
            LocalTime verlassenUm = parseZeit(dto.getVerlassenUm());
            if (ankunftszeit != null && verlassenUm != null && verlassenUm.isBefore(ankunftszeit)) {
                throw new IllegalArgumentException(student.getVorname() + " " + student.getName()
                        + ": Die Verlassen-Zeit liegt vor der Ankunftszeit.");
            }
            String kommentar = dto.getKommentar() == null || dto.getKommentar().isBlank()
                    ? null : dto.getKommentar().trim();
            eintraege.add(new GeprueftEintrag(student, status, ankunftszeit, verlassenUm, kommentar));
        }

        // Bestehende Erfassungen von heute aktualisieren, fehlende neu anlegen
        Map<Integer, Erfassung> heutigeErfassungen = findByGruppeUndMonat(gruppeId, heute, heute).stream()
                .collect(Collectors.toMap(e -> e.getStudenten().getId(), Function.identity(), (erste, zweite) -> erste));
        List<Erfassung> erfassungenToSave = new ArrayList<>();
        for (GeprueftEintrag eintrag : eintraege) {
            Erfassung erfassung = heutigeErfassungen.get(eintrag.student().getId());
            if (erfassung == null) {
                erfassung = new Erfassung(eintrag.student(), heute, eintrag.status(), eintrag.kommentar());
            }
            erfassung.setStatus(eintrag.status());
            erfassung.setKommentar(eintrag.kommentar());
            erfassung.setAnkunftszeit(eintrag.ankunftszeit());
            erfassung.setVerlassenUm(eintrag.verlassenUm());
            erfassungenToSave.add(erfassung);
        }
        saveAll(erfassungenToSave);
    }

    // Ein bereits geprüfter Formulareintrag
    private record GeprueftEintrag(Studenten student, Status status, LocalTime ankunftszeit,
                                   LocalTime verlassenUm, String kommentar) {
    }

    private static LocalTime parseZeit(String zeit) {
        return zeit == null || zeit.isBlank() ? null : LocalTime.parse(zeit.trim(), ZEIT_FORMAT);
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
