package com.art.erfassung.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO zur Übertragung von Gruppendaten.
 * <p>
 * Diese Klasse dient als Data Transfer Object für Gruppendaten zwischen
 * Service-Schicht und Präsentationsschicht. Sie enthält die wesentlichen
 * Attribute einer Gruppe, die für die Anzeige oder Bearbeitung benötigt werden.
 * </p>
 */
public class GruppeDTO {

    private Integer id;

    @NotNull(message = "Bezeichnung darf nicht null sein")
    @Size(min = 1, max = 100, message = "Bezeichnung muss zwischen 1 und 100 Zeichen lang sein")
    private String bezeichnung;

    /**
     * Gibt die ID der Gruppe zurück.
     *
     * @return die ID der Gruppe
     */
    public Integer getId() {
        return id;
    }

    /**
     * Setzt die ID der Gruppe.
     *
     * @param id die zu setzende ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gibt die Bezeichnung der Gruppe zurück.
     *
     * @return die Bezeichnung der Gruppe
     */
    public String getBezeichnung() {
        return bezeichnung;
    }

    /**
     * Setzt die Bezeichnung der Gruppe.
     *
     * @param bezeichnung die zu setzende Bezeichnung
     */
    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }
}