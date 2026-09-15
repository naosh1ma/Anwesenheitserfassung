package com.art.erfassung.dto;

import com.art.erfassung.model.Rolle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Formularobjekt zum Anlegen eines neuen Benutzers in der Benutzerverwaltung.
 */
public class BenutzerForm {

    @NotBlank(message = "Benutzername ist erforderlich")
    private String benutzername;

    @NotBlank(message = "Vorname ist erforderlich")
    private String vorname;

    @NotBlank(message = "Nachname ist erforderlich")
    private String name;

    @NotNull(message = "Rolle ist erforderlich")
    private Rolle rolle = Rolle.TEACHER;

    @NotNull(message = "Passwort ist erforderlich")
    @Size(min = 8, message = "Das Passwort muss mindestens 8 Zeichen lang sein.")
    private String passwort;

    public String getBenutzername() {
        return benutzername;
    }

    public void setBenutzername(String benutzername) {
        this.benutzername = benutzername;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rolle getRolle() {
        return rolle;
    }

    public void setRolle(Rolle rolle) {
        this.rolle = rolle;
    }

    public String getPasswort() {
        return passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }
}
