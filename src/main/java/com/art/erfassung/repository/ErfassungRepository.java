package com.art.erfassung.repository;

import com.art.erfassung.model.Erfassung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository zur Verwaltung von Erfassungen.
 * <p>
 * Dieses Repository erweitert das {@link JpaRepository} und stellt neben den
 * Standard-CRUD-Operationen benutzerdefinierte Methoden zur Abfrage von Erfassungsdaten
 * zur Verfügung. Die Methoden basieren auf Namenskonventionen und ermöglichen es, Erfassungen
 * anhand von Studenten- und Gruppeninformationen sowie Datumseinschränkungen zu filtern.
 */
@Repository
public interface ErfassungRepository extends JpaRepository<Erfassung, Integer> {

    /**
     * Sucht alle Erfassungen, die einem bestimmten Studenten zugeordnet sind.
     *
     * @param studenten_Id die ID des Studenten, dessen Erfassungen gesucht werden
     * @return eine Liste von {@link Erfassung} Objekten, die dem angegebenen Studenten zugeordnet sind
     */
    List<Erfassung> findByStudenten_id(Integer studenten_Id);

    /**
     * Sucht alle Erfassungen, die zu einer bestimmten Gruppe gehören und deren Datum
     * innerhalb eines angegebenen Zeitraums liegt.
     *
     * @param studenten_gruppe_id die ID der Gruppe, deren Erfassungen gesucht werden
     * @param datum               das Startdatum des gewünschten Zeitraums
     * @param datum2              das Enddatum des gewünschten Zeitraums
     * @return eine Liste von {@link Erfassung} Objekten, die den Kriterien entsprechen
     */
    List<Erfassung> findByStudenten_GruppeIdAndDatumBetween(Integer studenten_gruppe_id, LocalDate datum, LocalDate datum2);

    /**
     * Sucht eine Erfassung für einen bestimmten Studenten an einem gegebenen Datum.
     *
     * @param studentId die ID des Studenten, dessen Erfassung gesucht wird
     * @param datum     das Datum, für das die Erfassung gesucht wird
     * @return ein {@link Optional} mit dem gefundenen {@link Erfassung} Objekt,
     *         oder ein leeres Optional, falls keine Erfassung gefunden wird
     */
    Optional<Erfassung> findByStudenten_IdAndDatum(Integer studentId, LocalDate datum);

}
