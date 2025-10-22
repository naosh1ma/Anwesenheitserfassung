package com.art.erfassung.error;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

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
        StringBuilder errorMessage = new StringBuilder("Validierungsfehler:<br>");
        ex.getConstraintViolations().forEach(violation -> 
            errorMessage.append("• ").append(violation.getMessage()).append("<br>")
        );
        model.addAttribute("errorMessage", errorMessage.toString());
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
        model.addAttribute("errorMessage", "Sie haben keine Berechtigung für diese Aktion.");
        model.addAttribute("errorType", "access_denied");
        return "error";
    }

    /**
     * Behandelt alle weiteren, unerwarteten Exceptions.
     *
     * @param ex die ausgelöste Exception
     * @param model das Model für die View
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        logger.error("Ein unerwarteter Fehler ist aufgetreten: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten. Bitte kontaktieren Sie den Administrator.");
        model.addAttribute("errorType", "internal");
        return "error";
    }

    /**
     * Behandelt Exceptions in Redirect-Szenarien (z.B. nach Formular-Submission).
     *
     * @param ex die ausgelöste Exception
     * @param redirectAttributes Redirect-Attribute für Flash-Messages
     * @return Redirect-String zur vorherigen Seite
     */
    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
    public String handleRedirectExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        logger.warn("Fehler bei Redirect: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Fehler: " + ex.getMessage());
        return "redirect:/gruppen";
    }
}
