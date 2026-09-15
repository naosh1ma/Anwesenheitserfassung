package com.art.erfassung.tests;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.repository.StudentenRepository;
import com.art.erfassung.service.GruppeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link GruppeService}.
 */
public class GruppeServiceTest {

    @Mock
    private GruppeRepository gruppeRepository;

    @Mock
    private StudentenRepository studentenRepository;

    private GruppeService gruppeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gruppeService = new GruppeService(gruppeRepository, studentenRepository);
    }

    @Test
    public void testFindAll_SortedByName() {
        // Arrange
        Gruppe gruppe1 = new Gruppe("Gruppe 1");
        gruppe1.setId(1);
        Gruppe gruppe2 = new Gruppe("Gruppe 2");
        gruppe2.setId(2);

        when(gruppeRepository.findAll()).thenReturn(Arrays.asList(gruppe2, gruppe1));

        // Act
        List<Gruppe> actualGruppen = gruppeService.findAll();

        // Assert
        assertEquals(List.of(gruppe1, gruppe2), actualGruppen);
        verify(gruppeRepository, times(1)).findAll();
    }

    @Test
    public void testFindOrThrow_ExistingId() {
        // Arrange
        Integer id = 1;
        Gruppe expectedGruppe = new Gruppe("Test Gruppe");
        expectedGruppe.setId(id);

        when(gruppeRepository.findById(id)).thenReturn(Optional.of(expectedGruppe));

        // Act
        Gruppe actualGruppe = gruppeService.findOrThrow(id);

        // Assert
        assertEquals(expectedGruppe, actualGruppe);
        verify(gruppeRepository, times(1)).findById(id);
    }

    @Test
    public void testFindOrThrow_NonExistingId() {
        // Arrange
        Integer id = 999;
        when(gruppeRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            gruppeService.findOrThrow(id);
        });
        verify(gruppeRepository, times(1)).findById(id);
    }

    @Test
    public void testAnlegen_TrimsAndSaves() {
        // Arrange
        when(gruppeRepository.findAllByBezeichnungIgnoreCase("Informatik 1A")).thenReturn(List.of());
        when(gruppeRepository.save(any(Gruppe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Gruppe gruppe = gruppeService.anlegen("  Informatik 1A ");

        // Assert
        assertEquals("Informatik 1A", gruppe.getBezeichnung());
    }

    @Test
    public void testAnlegen_BlankName_Throws() {
        assertThrows(IllegalArgumentException.class, () -> gruppeService.anlegen("   "));
        verify(gruppeRepository, never()).save(any());
    }

    @Test
    public void testAnlegen_DuplicateName_Throws() {
        // Arrange
        Gruppe vorhanden = new Gruppe("Informatik 1A");
        vorhanden.setId(1);
        when(gruppeRepository.findAllByBezeichnungIgnoreCase("informatik 1a")).thenReturn(List.of(vorhanden));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> gruppeService.anlegen("informatik 1a"));
        verify(gruppeRepository, never()).save(any());
    }

    @Test
    public void testUmbenennen_SameNameForSameGroup_IsAllowed() {
        // Arrange
        Gruppe gruppe = new Gruppe("Informatik 1A");
        gruppe.setId(1);
        when(gruppeRepository.findById(1)).thenReturn(Optional.of(gruppe));
        when(gruppeRepository.findAllByBezeichnungIgnoreCase("Informatik 1A")).thenReturn(List.of(gruppe));

        // Act
        gruppeService.umbenennen(1, "Informatik 1A");

        // Assert
        verify(gruppeRepository).save(gruppe);
    }

    @Test
    public void testLoeschen_WithStudents_Throws() {
        // Arrange
        Gruppe gruppe = new Gruppe("Informatik 1A");
        gruppe.setId(1);
        when(gruppeRepository.findById(1)).thenReturn(Optional.of(gruppe));
        when(studentenRepository.countByGruppeId(1)).thenReturn(2L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> gruppeService.loeschen(1));
        verify(gruppeRepository, never()).delete(any());
    }

    @Test
    public void testLoeschen_EmptyGroup_Deletes() {
        // Arrange
        Gruppe gruppe = new Gruppe("Leere Gruppe");
        gruppe.setId(1);
        when(gruppeRepository.findById(1)).thenReturn(Optional.of(gruppe));
        when(studentenRepository.countByGruppeId(1)).thenReturn(0L);

        // Act
        gruppeService.loeschen(1);

        // Assert
        verify(gruppeRepository).delete(gruppe);
    }
}
