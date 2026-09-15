package com.art.erfassung.tests;

import com.art.erfassung.model.Benutzer;
import com.art.erfassung.model.Rolle;
import com.art.erfassung.repository.BenutzerRepository;
import com.art.erfassung.service.BenutzerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link BenutzerService}.
 */
public class BenutzerServiceTest {

    @Mock
    private BenutzerRepository benutzerRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private BenutzerService benutzerService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        benutzerService = new BenutzerService(benutzerRepository, passwordEncoder);
    }

    @Test
    public void testMigrierePasswoerter_HashesPlaintextAndSetsTeacherRole() {
        // Arrange
        Benutzer alt = new Benutzer("alt", "Anton", "Alt", null);
        alt.setPasswort("klartext");
        Benutzer neu = new Benutzer("neu", "Nina", "Neu", Rolle.ADMIN);
        neu.setPasswort(passwordEncoder.encode("geheim123"));
        String hashVorher = neu.getPasswort();
        when(benutzerRepository.findAll()).thenReturn(List.of(alt, neu));

        // Act
        int aktualisiert = benutzerService.migrierePasswoerter();

        // Assert
        assertEquals(1, aktualisiert);
        assertNotEquals("klartext", alt.getPasswort());
        assertTrue(passwordEncoder.matches("klartext", alt.getPasswort()));
        assertEquals(Rolle.TEACHER, alt.getRolle());
        assertEquals(hashVorher, neu.getPasswort());
        verify(benutzerRepository).saveAll(List.of(alt));
    }

    @Test
    public void testMigrierePasswoerter_NothingToDo() {
        // Arrange
        Benutzer benutzer = new Benutzer("neu", "Nina", "Neu", Rolle.TEACHER);
        benutzer.setPasswort(passwordEncoder.encode("geheim123"));
        when(benutzerRepository.findAll()).thenReturn(List.of(benutzer));

        // Act & Assert
        assertEquals(0, benutzerService.migrierePasswoerter());
        verify(benutzerRepository, never()).saveAll(any());
    }

    @Test
    public void testErstelleAdminFallsNoetig_CreatesAdmin() {
        // Arrange
        when(benutzerRepository.existsByRolle(Rolle.ADMIN)).thenReturn(false);
        when(benutzerRepository.findByBenutzername("chef")).thenReturn(Optional.empty());

        // Act
        boolean angelegt = benutzerService.erstelleAdminFallsNoetig("chef", "sicheres-passwort");

        // Assert
        assertTrue(angelegt);
        ArgumentCaptor<Benutzer> captor = ArgumentCaptor.forClass(Benutzer.class);
        verify(benutzerRepository).save(captor.capture());
        assertEquals("chef", captor.getValue().getBenutzername());
        assertEquals(Rolle.ADMIN, captor.getValue().getRolle());
        assertTrue(passwordEncoder.matches("sicheres-passwort", captor.getValue().getPasswort()));
    }

    @Test
    public void testErstelleAdminFallsNoetig_AdminExists_DoesNothing() {
        when(benutzerRepository.existsByRolle(Rolle.ADMIN)).thenReturn(true);

        assertFalse(benutzerService.erstelleAdminFallsNoetig("chef", "sicheres-passwort"));
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    public void testErstelleAdminFallsNoetig_NotConfigured_DoesNothing() {
        when(benutzerRepository.existsByRolle(Rolle.ADMIN)).thenReturn(false);

        assertFalse(benutzerService.erstelleAdminFallsNoetig("", ""));
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    public void testErstelleAdminFallsNoetig_PasswordTooShort_DoesNothing() {
        when(benutzerRepository.existsByRolle(Rolle.ADMIN)).thenReturn(false);

        assertFalse(benutzerService.erstelleAdminFallsNoetig("chef", "kurz"));
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    public void testLoadUserByUsername_MapsRole() {
        // Arrange
        Benutzer admin = new Benutzer("chef", "Carla", "Chef", Rolle.ADMIN);
        admin.setPasswort(passwordEncoder.encode("sicheres-passwort"));
        when(benutzerRepository.findByBenutzername("chef")).thenReturn(Optional.of(admin));

        // Act
        UserDetails details = benutzerService.loadUserByUsername("chef");

        // Assert
        assertEquals("chef", details.getUsername());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    public void testLoadUserByUsername_WithoutPassword_IsDisabled() {
        when(benutzerRepository.findByBenutzername("leer"))
                .thenReturn(Optional.of(new Benutzer("leer", "Lea", "Leer", Rolle.TEACHER)));

        assertFalse(benutzerService.loadUserByUsername("leer").isEnabled());
    }

    @Test
    public void testLoadUserByUsername_Unknown_Throws() {
        when(benutzerRepository.findByBenutzername("unbekannt")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> benutzerService.loadUserByUsername("unbekannt"));
    }

    @Test
    public void testAnlegen_DuplicateUsername_Throws() {
        when(benutzerRepository.findByBenutzername("chef"))
                .thenReturn(Optional.of(new Benutzer("chef", "Carla", "Chef", Rolle.ADMIN)));

        assertThrows(IllegalArgumentException.class,
                () -> benutzerService.anlegen("chef", "Carl", "Chef", Rolle.TEACHER, "sicheres-passwort"));
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    public void testLoeschen_OwnAccount_Throws() {
        Benutzer admin = new Benutzer("chef", "Carla", "Chef", Rolle.ADMIN);
        when(benutzerRepository.findById(1)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> benutzerService.loeschen(1, "chef"));
        verify(benutzerRepository, never()).delete(any());
    }
}
