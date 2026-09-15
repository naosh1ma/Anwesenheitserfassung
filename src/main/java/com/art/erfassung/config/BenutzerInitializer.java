package com.art.erfassung.config;

import com.art.erfassung.service.BenutzerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

/**
 * Bereitet die Benutzerdaten beim Start der Anwendung vor.
 * <p>
 * Zuerst werden noch im Klartext gespeicherte Passwörter gehasht, danach wird bei Bedarf
 * der erste Administrator aus den Umgebungsvariablen ADMIN_USERNAME und ADMIN_PASSWORD angelegt.
 * </p>
 */
@Component
public class BenutzerInitializer implements ApplicationRunner {

    private final BenutzerService benutzerService;
    private final String adminBenutzername;
    private final String adminPasswort;

    public BenutzerInitializer(BenutzerService benutzerService,
                               @Value("${app.admin.username:}") String adminBenutzername,
                               @Value("${app.admin.password:}") String adminPasswort) {
        this.benutzerService = benutzerService;
        this.adminBenutzername = adminBenutzername;
        this.adminPasswort = adminPasswort;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            benutzerService.migrierePasswoerter();
        } catch (DataAccessException | TransactionException e) {
            throw new IllegalStateException("Bestehende Passwörter konnten nicht gehasht werden. "
                    + "Prüfen Sie, ob die Spalte benutzer.passwort mindestens 60 Zeichen aufnehmen kann.", e);
        }
        benutzerService.erstelleAdminFallsNoetig(adminBenutzername, adminPasswort);
    }
}
