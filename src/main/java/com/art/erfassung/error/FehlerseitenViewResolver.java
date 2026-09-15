package com.art.erfassung.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Ergänzt Fehlerseiten, die Spring selbst erzeugt, um eine verständliche Meldung.
 * <p>
 * Betrifft Fehler, die außerhalb der Controller entstehen und daher nicht vom {@link GlobalExceptionHandler}
 * behandelt werden, z. B. ein 403, wenn ein Lehrer eine Seite für Administratoren aufruft, oder eine
 * abgelaufene Sitzung (ungültiges CSRF-Token).
 * </p>
 */
@Component
public class FehlerseitenViewResolver implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        Map<String, Object> fehlerseite = new HashMap<>(model);
        fehlerseite.putIfAbsent("errorMessage", FehlerMeldungen.meldung(status.value()));
        fehlerseite.putIfAbsent("errorType", FehlerMeldungen.typ(status.value()));
        return new ModelAndView("error", fehlerseite, status);
    }
}
