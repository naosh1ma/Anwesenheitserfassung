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
     * Sucht alle Studenten, die der angegebenen Gruppe zugeordnet sind, aktive und deaktivierte.
     *
     * @param gruppe_id die ID der Gruppe, deren Studenten gesucht werden
     * @return eine Liste von {@link Studenten} Objekten, die der angegebenen Gruppe angehören
     */
    List<Studenten> findByGruppeId(Integer gruppe_id);

    /**
     * Sucht alle aktiven (nicht deaktivierten) Studenten einer Gruppe.
     *
     * @param gruppeId die ID der Gruppe
     * @return die aktiven Studenten der Gruppe
     */
    List<Studenten> findByGruppeIdAndDeaktiviertAmIsNull(Integer gruppeId);

    /**
     * Zählt alle Studenten einer Gruppe, aktive und deaktivierte.
     *
     * @param gruppeId die ID der Gruppe
     * @return die Anzahl der Studenten
     */
    long countByGruppeId(Integer gruppeId);
}
