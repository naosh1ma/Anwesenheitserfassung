package com.art.erfassung.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Stellt dem Layout bereit, welcher Menüpunkt zur aktuellen Seite gehört.
 * <p>
 * Das Layout hebt den passenden Eintrag in der Kopfzeile hervor ("gruppen", "verwaltung" oder "benutzer").
 * </p>
 */
@ControllerAdvice
public class NavigationAdvice {

    @ModelAttribute("aktiveNavigation")
    public String aktiveNavigation(HttpServletRequest request) {
        String pfad = request.getRequestURI().substring(request.getContextPath().length());
        if (pfad.startsWith("/admin/gruppen")) {
            return "verwaltung";
        }
        if (pfad.startsWith("/admin/benutzer")) {
            return "benutzer";
        }
        return "gruppen";
    }
}
