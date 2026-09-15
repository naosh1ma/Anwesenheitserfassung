package com.art.erfassung.service;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.BenutzerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prüft, welche Gruppen ein angemeldeter Benutzer sehen darf.
 * <p>
 * Administratoren sehen alle Gruppen. Lehrer sehen nur die Gruppen, die ihnen in der Benutzerverwaltung
 * zugewiesen wurden, und können nur für diese Anwesenheit erfassen, Monatsübersichten und Statistiken öffnen.
 * </p>
 */
@Service
public class ZugriffService {

    // Service zum Laden aller oder der zugewiesenen Gruppen
    private final GruppeService gruppeService;
    // Repository zur Prüfung der Gruppenzuweisung
    private final BenutzerRepository benutzerRepository;

    public ZugriffService(GruppeService gruppeService, BenutzerRepository benutzerRepository) {
        this.gruppeService = gruppeService;
        this.benutzerRepository = benutzerRepository;
    }

    /**
     * Prüft, ob der angemeldete Benutzer Administrator ist.
     *
     * @param authentication der angemeldete Benutzer
     * @return {@code true} für Administratoren
     */
    public static boolean istAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    /**
     * Liefert die Gruppen, die der angemeldete Benutzer sehen darf, sortiert nach Bezeichnung.
     *
     * @param authentication der angemeldete Benutzer
     * @return alle Gruppen für Administratoren, sonst die zugewiesenen Gruppen
     */
    public List<Gruppe> sichtbareGruppen(Authentication authentication) {
        return istAdmin(authentication)
                ? gruppeService.findAll()
                : gruppeService.findZugewiesene(authentication.getName());
    }

    /**
     * Stellt sicher, dass der angemeldete Benutzer auf eine Gruppe zugreifen darf.
     *
     * @param gruppeId       die ID der Gruppe
     * @param authentication der angemeldete Benutzer
     * @throws AccessDeniedException wenn die Gruppe einem Lehrer nicht zugewiesen ist
     */
    public void pruefeGruppe(Integer gruppeId, Authentication authentication) {
        if (!istAdmin(authentication) && !benutzerRepository.hatGruppe(authentication.getName(), gruppeId)) {
            throw new AccessDeniedException("Die Gruppe " + gruppeId + " ist " + authentication.getName() + " nicht zugewiesen.");
        }
    }
}
