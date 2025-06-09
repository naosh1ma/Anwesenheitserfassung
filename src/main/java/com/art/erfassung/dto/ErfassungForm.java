package com.art.erfassung.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ErfassungForm {

    @NotEmpty(message = "Es müssen mindestens Einträge vorhanden sein.")
    @Valid
    private List<ErfassungDTO> eintraege;

    public List<ErfassungDTO> getEintraege() {
        return eintraege;
    }

    public void setEintraege(List<ErfassungDTO> eintraege) {
        this.eintraege = eintraege;
    }
}
