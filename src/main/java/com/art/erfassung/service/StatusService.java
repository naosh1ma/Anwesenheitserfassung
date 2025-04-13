package com.art.erfassung.service;

import com.art.erfassung.model.Status;
import com.art.erfassung.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Serviceklasse zur Verwaltung von Status-Objekten.
 * <p>
 * Diese Klasse bietet Methoden zum Abrufen aller Statusobjekte sowie zum
 * Suchen eines bestimmten Status anhand seiner ID. Dabei wird das {@link StatusRepository}
 * verwendet, um den Datenzugriff zu realisieren.
 */
@Service
public class StatusService {

    // Repository zur Verwaltung der Statusdaten
    private final StatusRepository statusRepository;

    @Autowired
    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    /**
     * Gibt eine Liste aller verfügbaren Status zurück.
     *
     * @return eine Liste von {@link Status} Objekten
     */
    public List<Status> findAll() {
        // Abrufen aller Status-Objekte aus dem Repository
        return statusRepository.findAll();
    }

    /**
     * Sucht einen Status anhand seiner ID.
     * <p>
     * Wird kein Status mit der angegebenen ID gefunden, wird eine NoSuchElementException geworfen.
     *
     * @param id die ID des gesuchten Status
     * @return das gefundene {@link Status} Objekt
     * @throws NoSuchElementException wenn kein Status mit der angegebenen ID existiert
     */
    public Status findOrThrow(Integer id) {
        // Suche nach dem Status, andernfalls Exception werfen
        return statusRepository.findById(id).orElseThrow();
    }
}
