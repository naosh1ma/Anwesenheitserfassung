package com.art.erfassung.dto;

import jakarta.validation.constraints.NotNull;

public class AnwesenheitDTO {

    @NotNull(message = "Studenten-ID darf nicht null sein")
    private Integer studentenId;

    @NotNull(message = "Status-ID darf nicht null sein")
    private Integer statusId;

    private String studentenName;

    private String ankunftszeit; // Format "HH:mm"
    private String verlassenUm;  // Format "HH:mm"
    private String kommentar;

    public Integer getStudentenId() {
        return studentenId;
    }

    public void setStudentenId(Integer studentenId) {
        this.studentenId = studentenId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getStudentenName() {
        return studentenName;}

    public void setStudentenName(String studentenName) {
        this.studentenName = studentenName;
    }

    public String getAnkunftszeit() {
        return ankunftszeit;
    }

    public void setAnkunftszeit(String ankunftszeit) {
        this.ankunftszeit = ankunftszeit;
    }

    public String getVerlassenUm() {
        return verlassenUm;
    }

    public void setVerlassenUm(String verlassenUm) {
        this.verlassenUm = verlassenUm;
    }

    public String getKommentar() {
        return kommentar;
    }

    public void setKommentar(String kommentar) {
        this.kommentar = kommentar;
    }
}
