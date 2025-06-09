package com.art.erfassung.mapper;

import com.art.erfassung.dto.ErfassungListeDTO;
import com.art.erfassung.model.Erfassung;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper-Klasse zur Konvertierung zwischen Erfassung-Entitäten und ErfassungListeDTO-Objekten.
 * <p>
 * Diese Klasse stellt Methoden bereit, um Erfassung-Entitäten in ErfassungListeDTO-Objekte
 * zu konvertieren. Sie wird verwendet, um die Trennung zwischen Datenzugriffsschicht und
 * Präsentationsschicht bei der Anzeige von Anwesenheitslisten zu gewährleisten.
 * </p>
 */
@Component
public class ErfassungListeMapper {

    /**
     * Konvertiert eine Erfassung-Entität in ein ErfassungListeDTO-Objekt.
     *
     * @param erfassung die zu konvertierende Erfassung-Entität
     * @return das resultierende ErfassungListeDTO-Objekt
     */
    public ErfassungListeDTO toDTO(Erfassung erfassung) {
        if (erfassung == null) {
            return null;
        }

        ErfassungListeDTO dto = new ErfassungListeDTO();
        dto.setId(erfassung.getId());
        dto.setDatum(erfassung.getDatum());
        dto.setKommentar(erfassung.getKommentar());
        
        if (erfassung.getStudenten() != null) {
            dto.setStudentenId(erfassung.getStudenten().getId());
            dto.setStudentenName(erfassung.getStudenten().getVorname() + " " + erfassung.getStudenten().getName());
        }
        
        if (erfassung.getStatus() != null) {
            dto.setStatusId(erfassung.getStatus().getId());
            dto.setStatusBezeichnung(erfassung.getStatus().getBezeichnung());
        }
        
        return dto;
    }

    /**
     * Konvertiert eine Liste von Erfassung-Entitäten in eine Liste von ErfassungListeDTO-Objekten.
     *
     * @param erfassungen die zu konvertierende Liste von Erfassung-Entitäten
     * @return die resultierende Liste von ErfassungListeDTO-Objekten
     */
    public List<ErfassungListeDTO> toDTOList(List<Erfassung> erfassungen) {
        if (erfassungen == null) {
            return null;
        }

        return erfassungen.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}