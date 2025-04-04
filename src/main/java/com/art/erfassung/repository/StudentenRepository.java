package com.art.erfassung.repository;

import com.art.erfassung.model.Studenten;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository zur Verwaltung von Studenten-Entitäten.
 * <p>
 * Dieses Interface erweitert {@link JpaRepository} und stellt somit grundlegende CRUD-Operationen
 * für die {@link Studenten} Entität bereit.
 * </p>
 */
@Repository
public interface StudentenRepository extends JpaRepository<Studenten, Integer> {
    /**
     * Sucht alle Studenten, die der angegebenen Gruppe zugeordnet sind.
     *
     * @param gruppe_id die ID der Gruppe, deren Studenten gesucht werden
     * @return eine Liste von {@link Studenten} Objekten, die der angegebenen Gruppe angehören
     */
    List<Studenten> findByGruppeId(Integer gruppe_id);
}
