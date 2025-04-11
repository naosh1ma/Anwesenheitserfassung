package com.art.erfassung.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AnwesenheitsForm {

    @NotEmpty(message = "Es müssen mindestens Einträge vorhanden sein.")
    @Valid
    private List<AnwesenheitsDTO> eintraege;

    public List<AnwesenheitsDTO> getEintraege() {
        return eintraege;
    }

    public void setEintraege(List<AnwesenheitsDTO> eintraege) {
        this.eintraege = eintraege;
    }
}
