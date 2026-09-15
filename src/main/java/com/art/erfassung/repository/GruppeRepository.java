package com.art.erfassung.repository;

import com.art.erfassung.model.Gruppe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository zur Verwaltung von Gruppen-Entitäten.
 * <p>
 * Dieses Interface erweitert {@link JpaRepository} und stellt somit grundlegende CRUD-Operationen
 * für die {@link Gruppe} Entität bereit.
 */
@Repository
public interface GruppeRepository extends JpaRepository<Gruppe, Integer> {

    /**
     * Sucht Gruppen mit der angegebenen Bezeichnung, unabhängig von Groß- und Kleinschreibung.
     *
     * @param bezeichnung die gesuchte Bezeichnung
     * @return die Gruppen mit dieser Bezeichnung
     */
    List<Gruppe> findAllByBezeichnungIgnoreCase(String bezeichnung);
}
