package com.art.erfassung.mapper;

import com.art.erfassung.dto.StudentenDTO;
import com.art.erfassung.model.Studenten;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper-Klasse zur Konvertierung zwischen Studenten-Entitäten und DTOs.
 * <p>
 * Diese Klasse stellt Methoden bereit, um Studenten-Entitäten in StudentenDTO-Objekte
 * und umgekehrt zu konvertieren. Sie wird verwendet, um die Trennung zwischen
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

    /**
     * Konvertiert ein StudentenDTO-Objekt in eine Studenten-Entität.
     * Hinweis: Die Gruppe-Referenz muss separat gesetzt werden, da das DTO nur die ID enthält.
     *
     * @param dto das zu konvertierende StudentenDTO-Objekt
     * @return die resultierende Studenten-Entität
     */
    public Studenten toEntity(StudentenDTO dto) {
        if (dto == null) {
            return null;
        }

        Studenten student = new Studenten();
        student.setId(dto.getId());
        student.setName(dto.getName());
        student.setVorname(dto.getVorname());
        
        // Die Gruppe muss separat gesetzt werden, da das DTO nur die ID enthält
        
        return student;
    }

    /**
     * Konvertiert eine Liste von Studenten-Entitäten in eine Liste von StudentenDTO-Objekten.
     *
     * @param studenten die zu konvertierende Liste von Studenten-Entitäten
     * @return die resultierende Liste von StudentenDTO-Objekten
     */
    public List<StudentenDTO> toDTOList(List<Studenten> studenten) {
        if (studenten == null) {
            return null;
        }

        return studenten.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Konvertiert eine Liste von StudentenDTO-Objekten in eine Liste von Studenten-Entitäten.
     * Hinweis: Die Gruppe-Referenzen müssen separat gesetzt werden.
     *
     * @param dtos die zu konvertierende Liste von StudentenDTO-Objekten
     * @return die resultierende Liste von Studenten-Entitäten
     */
    public List<Studenten> toEntityList(List<StudentenDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}