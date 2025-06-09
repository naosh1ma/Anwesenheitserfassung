package com.art.erfassung.tests;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.GruppeRepository;
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
import static org.mockito.Mockito.*;

/**
 * Test class for {@link GruppeService}.
 */
public class GruppeServiceTest {

    @Mock
    private GruppeRepository gruppeRepository;

    private GruppeService gruppeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gruppeService = new GruppeService(gruppeRepository);
    }

    @Test
    public void testFindAll() {
        // Arrange
        Gruppe gruppe1 = new Gruppe("Gruppe 1");
        gruppe1.setId(1);
        Gruppe gruppe2 = new Gruppe("Gruppe 2");
        gruppe2.setId(2);
        List<Gruppe> expectedGruppen = Arrays.asList(gruppe1, gruppe2);
        
        when(gruppeRepository.findAll()).thenReturn(expectedGruppen);

        // Act
        List<Gruppe> actualGruppen = gruppeService.findAll();

        // Assert
        assertEquals(expectedGruppen, actualGruppen);
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
}