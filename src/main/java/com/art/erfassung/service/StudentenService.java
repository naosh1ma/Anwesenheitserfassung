package com.art.erfassung.service;

import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Serviceklasse zur Verwaltung von Studenten.
 * <p>
 * Diese Klasse stellt Methoden zur Abfrage von Studenten bereit, entweder anhand der Gruppen-ID
 * oder durch Suche nach einer spezifischen Studenten-ID. Für den Datenzugriff wird das
 * {@link StudentenRepository} verwendet.
 */
@Service
public class StudentenService {

    // Repository zur Verwaltung der Studenten-Daten
    private final StudentenRepository studentenRepository;

    /**
     * Konstruktor zur Initialisierung des StudentenService mit dem erforderlichen Repository.
     *
     * @param studentenRepository das Repository, das für den Zugriff auf Studenten zuständig ist.
     */
    @Autowired
    public StudentenService(StudentenRepository studentenRepository) {
        this.studentenRepository = studentenRepository;
    }

    /**
     * Liefert eine Liste von Studenten, die der angegebenen Gruppe zugeordnet sind.
     *
     * @param gruppeId die ID der Gruppe, deren Studenten gesucht werden
     * @return eine Liste von {@link Studenten} Objekten, die der Gruppe angehören
     */
    public List<Studenten> findByGruppeId(Integer gruppeId) {
        // Abfrage der Studenten anhand der Gruppen-ID im Repository
        return studentenRepository.findByGruppeId(gruppeId);
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
}
