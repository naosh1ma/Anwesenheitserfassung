package com.art.erfassung.repository;

import com.art.erfassung.model.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository zur Verwaltung von Benutzer-Entitäten.
 * <p>
 * Dieses Repository erweitert {@link JpaRepository} und stellt somit grundlegende CRUD-Operationen
 * sowie benutzerdefinierte Abfragemethoden für die {@link Benutzer} Entität bereit.
 */
@Repository
public interface BenutzerRepository extends JpaRepository<Benutzer, Integer> {
    /**
     * Sucht einen Benutzer anhand seines Benutzernamens.
     *
     * @param benutzername der Benutzername, anhand dessen der Benutzer gesucht wird
     * @return ein {@link Optional} mit dem gefundenen {@link Benutzer} Objekt, oder
     *         ein leeres Optional, falls kein Benutzer mit dem angegebenen Benutzernamen existiert
     */
    Optional<Benutzer> findByBenutzername(String benutzername);

}

