package com.art.erfassung.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AnwesenheitForm {

    @NotEmpty(message = "Es müssen mindestens Einträge vorhanden sein.")
    @Valid
    private List<AnwesenheitDTO> eintraege;

    public List<AnwesenheitDTO> getEintraege() {
        return eintraege;
    }

    public void setEintraege(List<AnwesenheitDTO> eintraege) {
        this.eintraege = eintraege;
    }
}
