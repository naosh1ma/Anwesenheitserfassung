package com.art.erfassung.mapper;

import com.art.erfassung.dto.StudentenDTO;
import com.art.erfassung.model.Studenten;
import org.springframework.stereotype.Component;

/**
 * Mapper-Klasse zur Konvertierung von Studenten-Entitäten in DTOs.
 * <p>
 * Diese Klasse stellt eine Methode bereit, um Studenten-Entitäten in StudentenDTO-Objekte
 * zu konvertieren. Sie wird verwendet, um die Trennung zwischen
 * Präsentationsschicht und Datenzugriffsschicht zu gewährleisten.
 * </p>
 */
@Component
public class StudentenMapper {

    /**
     * Konvertiert eine Studenten-Entität in ein StudentenDTO-Objekt.
     *
     * @param student die zu konvertierende Studenten-Entität
     * @return das resultierende StudentenDTO-Objekt
     */
    public StudentenDTO toDTO(Studenten student) {
        if (student == null) {
            return null;
        }

        StudentenDTO dto = new StudentenDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setVorname(student.getVorname());

        if (student.getGruppe() != null) {
            dto.setGruppeId(student.getGruppe().getId());
            dto.setGruppeBezeichnung(student.getGruppe().getBezeichnung());
        }

        return dto;
    }
}
