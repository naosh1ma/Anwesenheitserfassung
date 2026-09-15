package com.art.erfassung.error;

/**
 * Benutzerfreundliche Fehlermeldungen und Fehlertypen je HTTP-Status.
 * <p>
 * Wird für Fehler aus Controllern ({@link GlobalExceptionHandler}) und für Fehler verwendet,
 * die Spring außerhalb der Controller erkennt, z. B. fehlende Berechtigungen ({@link FehlerseitenViewResolver}).
 * </p>
 */
final class FehlerMeldungen {

    private FehlerMeldungen() {
    }

    /**
     * Liefert die Meldung, die auf der Fehlerseite angezeigt wird.
     *
     * @param status der HTTP-Status
     * @return die Meldung
     */
    static String meldung(int status) {
        return switch (status) {
            case 400 -> "Die Anfrage war ungültig. Bitte prüfen Sie Ihre Eingaben.";
            case 403 -> "Sie haben keine Berechtigung für diese Seite oder Aktion, oder Ihre Sitzung ist abgelaufen.";
            case 404 -> "Die angeforderte Seite wurde nicht gefunden.";
            case 405 -> "Diese Aktion ist auf diesem Weg nicht möglich.";
            default -> "Ein unerwarteter Fehler ist aufgetreten. Bitte kontaktieren Sie den Administrator.";
        };
    }

    /**
     * Liefert den Fehlertyp, nach dem die Fehlerseite Symbol und Hilfetext auswählt.
     *
     * @param status der HTTP-Status
     * @return der Fehlertyp
     */
    static String typ(int status) {
        return switch (status) {
            case 400 -> "validation";
            case 403 -> "access_denied";
            case 404 -> "not_found";
            case 405 -> "request";
            default -> "internal";
        };
    }
}
