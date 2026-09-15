package com.art.erfassung.repository;

import com.art.erfassung.model.Benutzer;
import com.art.erfassung.model.Rolle;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Prüft, ob mindestens ein Benutzer mit der angegebenen Rolle existiert.
     *
     * @param rolle die gesuchte Rolle
     * @return {@code true}, wenn ein Benutzer mit dieser Rolle existiert
     */
    boolean existsByRolle(Rolle rolle);

    /**
     * Liefert alle Benutzer mitsamt ihren zugewiesenen Gruppen.
     *
     * @param sort die Sortierung
     * @return alle Benutzer
     */
    @EntityGraph(attributePaths = "gruppen")
    List<Benutzer> findAllBy(Sort sort);

    /**
     * Sucht einen Benutzer mitsamt seinen zugewiesenen Gruppen.
     *
     * @param id die ID des Benutzers
     * @return der Benutzer, falls vorhanden
     */
    @EntityGraph(attributePaths = "gruppen")
    Optional<Benutzer> findMitGruppenById(Integer id);

    /**
     * Prüft, ob einem Benutzer eine Gruppe zugewiesen ist.
     *
     * @param benutzername der Benutzername
     * @param gruppeId     die ID der Gruppe
     * @return {@code true}, wenn die Gruppe dem Benutzer zugewiesen ist
     */
    @Query("select case when count(g) > 0 then true else false end "
            + "from Benutzer b join b.gruppen g where b.benutzername = :benutzername and g.id = :gruppeId")
    boolean hatGruppe(@Param("benutzername") String benutzername, @Param("gruppeId") Integer gruppeId);
}
