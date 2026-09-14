package com.art.erfassung.repository;

import com.art.erfassung.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository zur Verwaltung von Status-Entitäten.
 * <p>
 * Dieses Interface erweitert {@link JpaRepository} und stellt somit grundlegende CRUD-Operationen
 * sowie weitere Datenzugriffsmethoden für die {@link Status} Entität bereit.
 * </p>
 */
@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByBezeichnung(String bezeichnung);
}
