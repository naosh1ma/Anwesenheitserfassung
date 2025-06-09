package com.art.erfassung.mapper;

import com.art.erfassung.dto.StatistikDTO;
import com.art.erfassung.service.StatistikService.StatistikErgebnis;
import org.springframework.stereotype.Component;

/**
 * Mapper-Klasse zur Konvertierung zwischen StatistikErgebnis-Records und StatistikDTO-Objekten.
 * <p>
 * Diese Klasse stellt Methoden bereit, um StatistikErgebnis-Records in StatistikDTO-Objekte
 * zu konvertieren. Sie wird verwendet, um die Trennung zwischen Service-Schicht und 
 * Präsentationsschicht zu gewährleisten.
 * </p>
 */
@Component
public class StatistikMapper {

    private final StudentenMapper studentenMapper;

    public StatistikMapper(StudentenMapper studentenMapper) {
        this.studentenMapper = studentenMapper;
    }

    /**
     * Konvertiert ein StatistikErgebnis-Record in ein StatistikDTO-Objekt.
     *
     * @param ergebnis das zu konvertierende StatistikErgebnis-Record
     * @return das resultierende StatistikDTO-Objekt
     */
    public StatistikDTO toDTO(StatistikErgebnis ergebnis) {
        if (ergebnis == null) {
            return null;
        }

        StatistikDTO dto = new StatistikDTO();
        
        // Konvertiere den Studenten in ein StudentenDTO, falls vorhanden
        if (ergebnis.student() != null) {
            dto.setStudent(studentenMapper.toDTO(ergebnis.student()));
        }
        
        dto.setGesamtAnwesenheit(ergebnis.gesamtAnwesenheit());
        dto.setEntschuldigt(ergebnis.entschuldigt());
        dto.setUnentschuldigt(ergebnis.unentschuldigt());
        dto.setKrank(ergebnis.krank());
        dto.setVerspaetungen(ergebnis.verspaetungen());
        
        return dto;
    }
}