package com.art.erfassung.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Globaler Exception Handler für die Erfassung-Anwendung.
 * <p>
 * Diese Klasse behandelt alle Exceptions, die in der Anwendung auftreten können,
 * und stellt benutzerfreundliche Fehlermeldungen bereit. Sie unterscheidet zwischen
 * verschiedenen Arten von Fehlern und reagiert entsprechend.
 * </p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Behandelt IllegalArgumentException - ungültige Eingaben oder Parameter.
     *
     * @param ex die ausgelöste IllegalArgumentException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        logger.warn("Ungültige Eingabe: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Ungültige Eingabe: " + ex.getMessage());
        model.addAttribute("errorType", "validation");
        return "error";
    }

    /**
     * Behandelt NoSuchElementException - gesuchte Ressourcen nicht gefunden.
     *
     * @param ex die ausgelöste NoSuchElementException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoSuchElementException(NoSuchElementException ex, Model model) {
        logger.warn("Ressource nicht gefunden: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Die angeforderte Ressource wurde nicht gefunden.");
        model.addAttribute("errorType", "not_found");
        return "error";
    }

    /**
     * Behandelt NoResourceFoundException - unbekannte URL oder fehlende Datei (z. B. favicon.ico).
     * <p>
     * Solche Anfragen kommen im normalen Betrieb häufig vor und werden daher nur auf Debug-Level protokolliert.
     * </p>
     *
     * @param ex die ausgelöste NoResourceFoundException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException ex, Model model) {
        logger.debug("Seite oder Datei nicht gefunden: {}", ex.getResourcePath());
        model.addAttribute("errorMessage", FehlerMeldungen.meldung(404));
        model.addAttribute("errorType", FehlerMeldungen.typ(404));
        return "error";
    }

    /**
     * Behandelt MethodArgumentTypeMismatchException - ungültiger Wert in der Adresse, z. B. "/anwesenheit/abc".
     *
     * @param ex die ausgelöste MethodArgumentTypeMismatchException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, Model model) {
        logger.warn("Ungültiger Wert für '{}': {}", ex.getName(), ex.getValue());
        model.addAttribute("errorMessage", FehlerMeldungen.meldung(400));
        model.addAttribute("errorType", FehlerMeldungen.typ(400));
        return "error";
    }

    /**
     * Behandelt DateTimeParseException - ungültige Datums-/Zeitformate.
     *
     * @param ex die ausgelöste DateTimeParseException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDateTimeParseException(DateTimeParseException ex, Model model) {
        logger.warn("Ungültiges Datums-/Zeitformat: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Ungültiges Datums- oder Zeitformat. Bitte verwenden Sie das Format HH:MM für Zeiten.");
        model.addAttribute("errorType", "format");
        return "error";
    }

    /**
     * Behandelt ConstraintViolationException - Validierungsfehler.
     *
     * @param ex die ausgelöste ConstraintViolationException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException ex, Model model) {
        logger.warn("Validierungsfehler: {}", ex.getMessage());
        String details = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        model.addAttribute("errorMessage", "Validierungsfehler: " + details);
        model.addAttribute("errorType", "validation");
        return "error";
    }

    /**
     * Behandelt DataAccessException - Datenbankfehler.
     *
     * @param ex die ausgelöste DataAccessException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDataAccessException(DataAccessException ex, Model model) {
        logger.error("Datenbankfehler: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Ein Datenbankfehler ist aufgetreten. Bitte versuchen Sie es später erneut.");
        model.addAttribute("errorType", "database");
        return "error";
    }

    /**
     * Behandelt AccessDeniedException - Zugriff verweigert.
     *
     * @param ex die ausgelöste AccessDeniedException
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model) {
        logger.warn("Zugriff verweigert: {}", ex.getMessage());
        model.addAttribute("errorMessage", FehlerMeldungen.meldung(403));
        model.addAttribute("errorType", FehlerMeldungen.typ(403));
        return "error";
    }

    /**
     * Behandelt alle weiteren Exceptions.
     * <p>
     * Anfragefehler, die Spring selbst erkennt (z. B. falsche HTTP-Methode oder fehlender Parameter),
     * behalten ihren HTTP-Status und werden nur als Warnung protokolliert. Alles andere ist ein
     * unerwarteter Fehler (Status 500).
     * </p>
     *
     * @param ex die ausgelöste Exception
     * @return die Fehlerseite mit passendem HTTP-Status
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            logger.warn("Ungültige Anfrage ({}): {}", status, ex.getMessage());
            return fehlerseite(status);
        }
        logger.error("Ein unerwarteter Fehler ist aufgetreten: {}", ex.getMessage(), ex);
        return fehlerseite(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private static ModelAndView fehlerseite(int status) {
        ModelAndView fehlerseite = new ModelAndView("error");
        fehlerseite.addObject("errorMessage", FehlerMeldungen.meldung(status));
        fehlerseite.addObject("errorType", FehlerMeldungen.typ(status));
        fehlerseite.setStatus(HttpStatusCode.valueOf(status));
        return fehlerseite;
    }
}
