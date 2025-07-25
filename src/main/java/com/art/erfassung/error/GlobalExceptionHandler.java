package com.art.erfassung.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Behandelt alle IllegalArgumentException, die im Anwendungskontext auftreten.
     * Solche Ausnahmen entstehen häufig, wenn ungültige Eingaben erfolgen.
     *
     * @param ex die ausgelöste IllegalArgumentException
     * @param model das Model, in das die Fehlermeldung geschrieben wird
     * @return den Namen der Fehlerseite (z. B. "error")
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    /**
     * Behandelt alle weiteren, unerwarteten Exceptions.
     *
     * @param ex die ausgelöste Exception
     * @param model das Model, in das die Fehlermeldung geschrieben wird
     * @return den Namen der Fehlerseite
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        logger.error("Ein unerwarteter Fehler ist aufgetreten" + ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
        return "error"; // Fehlerseite (error.html) anzeigen
    }
}
