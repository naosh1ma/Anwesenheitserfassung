package com.art.erfassung.service;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.GruppeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Serviceklasse zur Verwaltung von Gruppen.
 * <p>
 * Diese Klasse stellt Methoden zum Abrufen einzelner Gruppen sowie zur
 * Abfrage aller Gruppen zur Verfügung. Der Zugriff auf die Daten erfolgt über
 * das {@link GruppeRepository}.
 */
@Service
public class GruppeService {

    // Repository zur Verwaltung der Gruppen-Daten
    private final GruppeRepository gruppeRepository;

    /**
     * Konstruktor zur Initialisierung des GruppeService mit dem benötigten Repository.
     *
     * @param gruppeRepository das Repository, das für den Datenzugriff auf Gruppen zuständig ist.
     */
    @Autowired
    public GruppeService(GruppeRepository gruppeRepository) {
        this.gruppeRepository = gruppeRepository;
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
     * Liefert eine Liste aller vorhandenen Gruppen.
     *
     * @return eine Liste aller {@link Gruppe} Objekte
     */
    public List<Gruppe> findAll() {
        // Abrufen aller Gruppen aus dem Repository
        return gruppeRepository.findAll();
    }
}
