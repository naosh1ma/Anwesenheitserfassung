package com.art.erfassung.tests;

import com.art.erfassung.error.FehlerseitenViewResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link FehlerseitenViewResolver}.
 */
public class FehlerseitenViewResolverTest {

    private final FehlerseitenViewResolver resolver = new FehlerseitenViewResolver();

    @Test
    public void testResolveErrorView_Forbidden_AddsPermissionMessage() {
        // Act
        ModelAndView fehlerseite = resolver.resolveErrorView(new MockHttpServletRequest(), HttpStatus.FORBIDDEN,
                Map.of("status", 403));

        // Assert
        assertEquals("error", fehlerseite.getViewName());
        assertEquals(HttpStatus.FORBIDDEN, fehlerseite.getStatus());
        assertTrue(fehlerseite.getModel().get("errorMessage").toString().contains("keine Berechtigung"));
        assertEquals("access_denied", fehlerseite.getModel().get("errorType"));
        // Spring's own error attributes stay available to the template
        assertEquals(403, fehlerseite.getModel().get("status"));
    }

    @Test
    public void testResolveErrorView_OtherStatus_UsesGenericMessage() {
        // Act
        ModelAndView fehlerseite = resolver.resolveErrorView(new MockHttpServletRequest(), HttpStatus.SERVICE_UNAVAILABLE,
                Map.of());

        // Assert
        assertTrue(fehlerseite.getModel().get("errorMessage").toString().contains("unerwarteter Fehler"));
        assertEquals("internal", fehlerseite.getModel().get("errorType"));
    }
}
