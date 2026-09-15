package com.art.erfassung.mapper;

import com.art.erfassung.dto.GruppeDTO;
import com.art.erfassung.model.Gruppe;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper-Klasse zur Konvertierung von Gruppe-Entitäten in DTOs.
 * <p>
 * Diese Klasse stellt Methoden bereit, um Gruppe-Entitäten in GruppeDTO-Objekte
 * zu konvertieren. Sie wird verwendet, um die Trennung zwischen
 * Präsentationsschicht und Datenzugriffsschicht zu gewährleisten.
 * </p>
 */
@Component
public class GruppeMapper {

    /**
     * Konvertiert eine Gruppe-Entität in ein GruppeDTO-Objekt.
     *
     * @param gruppe die zu konvertierende Gruppe-Entität
     * @return das resultierende GruppeDTO-Objekt
     */
    public GruppeDTO toDTO(Gruppe gruppe) {
        if (gruppe == null) {
            return null;
        }

        GruppeDTO dto = new GruppeDTO();
        dto.setId(gruppe.getId());
        dto.setBezeichnung(gruppe.getBezeichnung());
        return dto;
    }

    /**
     * Konvertiert eine Liste von Gruppe-Entitäten in eine Liste von GruppeDTO-Objekten.
     *
     * @param gruppen die zu konvertierende Liste von Gruppe-Entitäten
     * @return die resultierende Liste von GruppeDTO-Objekten
     */
    public List<GruppeDTO> toDTOList(List<Gruppe> gruppen) {
        if (gruppen == null) {
            return null;
        }

        return gruppen.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
