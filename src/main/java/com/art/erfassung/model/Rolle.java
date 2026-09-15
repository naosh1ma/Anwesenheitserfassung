package com.art.erfassung.model;

/**
 * Rollen, die ein Benutzer haben kann.
 * <p>
 * Der Name der Konstante entspricht der Spring-Security-Rolle (z. B. {@code ROLE_ADMIN}).
 * </p>
 */
public enum Rolle {
    ADMIN("Administrator"),
    TEACHER("Lehrer");

    // Anzeigename in der Oberfläche
    private final String bezeichnung;

    Rolle(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
