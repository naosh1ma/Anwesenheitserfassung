package com.art.erfassung.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ErfassungDTO {

    @NotNull(message = "Studenten-ID ist erforderlich")
    private Integer studentenId;

    @NotNull(message = "Status-ID ist erforderlich")
    private Integer statusId;

    private String studentenName;

    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", 
             message = "Ankunftszeit muss im Format HH:MM angegeben werden (z.B. 08:30)")
    private String ankunftszeit; // Format "HH:mm"
    
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", 
             message = "Verlassen-Zeit muss im Format HH:MM angegeben werden (z.B. 16:30)")
    private String verlassenUm;  // Format "HH:mm"
    
    @Size(max = 500, message = "Kommentar darf maximal 500 Zeichen lang sein")
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
