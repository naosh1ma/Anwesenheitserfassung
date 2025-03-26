package com.art.erfassung.repository;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErfassungRepository extends JpaRepository<Erfassung, Integer> {

    List<Erfassung> findByStudenten_id(Integer studenten_Id);

    List<Erfassung> findByStudenten_GruppeIdAndDatumBetween(Integer studenten_gruppe_id, LocalDate datum, LocalDate datum2);

     @Query("SELECT e FROM Erfassung e WHERE e.studenten.gruppe.id = :gruppeId AND FUNCTION('YEAR', e.datum) = :jahr AND FUNCTION('MONTH', e.datum) = :monat")
    List<Erfassung> findByGruppeAndMonat(@Param("gruppeId") Integer gruppeId,
                                         @Param("jahr") int jahr,
                                         @Param("monat") int monat);


    Optional<Erfassung> findByStudenten_IdAndDatum(Integer studentId, LocalDate datum);
}
