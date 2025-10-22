package com.art.erfassung.tests;

import com.art.erfassung.dto.ErfassungDTO;
import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.StatusRepository;
import com.art.erfassung.repository.StudentenRepository;
import com.art.erfassung.service.ErfassungService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link ErfassungService}.
 */
public class ErfassungServiceTest {

    @Mock
    private ErfassungRepository erfassungRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private StudentenRepository studentenRepository;

    private ErfassungService erfassungService;

    private Gruppe testGruppe;
    private Studenten testStudent;
    private Status testStatus;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        erfassungService = new ErfassungService(erfassungRepository, statusRepository, studentenRepository);

        // Setup test data
        testGruppe = new Gruppe("Test Gruppe");
        testGruppe.setId(1);

        testStudent = new Studenten("Mustermann", "Max", testGruppe);
        testStudent.setId(1);

        testStatus = new Status();
        testStatus.setId(1);
        testStatus.setBezeichnung("Anwesend");
    }

    @Test
    public void testErfassenAnwesenheiten_NewErfassung() {
        // Arrange
        ErfassungDTO dto = new ErfassungDTO();
        dto.setStudentenId(1);
        dto.setStatusId(1);
        dto.setAnkunftszeit("08:30");
        dto.setKommentar("Test Kommentar");

        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(statusRepository.findById(1)).thenReturn(Optional.of(testStatus));
        when(erfassungRepository.findByStudenten_IdAndDatum(1, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(erfassungRepository.saveAll(any())).thenReturn(Arrays.asList());

        // Act
        Integer result = erfassungService.erfassenAnwesenheiten(Arrays.asList(dto));

        // Assert
        assertEquals(1, result);
        verify(studentenRepository).findById(1);
        verify(statusRepository).findById(1);
        verify(erfassungRepository).findByStudenten_IdAndDatum(1, LocalDate.now());
        verify(erfassungRepository).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_UpdateExistingErfassung() {
        // Arrange
        ErfassungDTO dto = new ErfassungDTO();
        dto.setStudentenId(1);
        dto.setStatusId(1);
        dto.setAnkunftszeit("08:30");
        dto.setKommentar("Aktualisierter Kommentar");

        Erfassung existingErfassung = new Erfassung(testStudent, LocalDate.now(), testStatus, "Alter Kommentar");

        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(statusRepository.findById(1)).thenReturn(Optional.of(testStatus));
        when(erfassungRepository.findByStudenten_IdAndDatum(1, LocalDate.now()))
                .thenReturn(Optional.of(existingErfassung));
        when(erfassungRepository.saveAll(any())).thenReturn(Arrays.asList(existingErfassung));

        // Act
        Integer result = erfassungService.erfassenAnwesenheiten(Arrays.asList(dto));

        // Assert
        assertEquals(1, result);
        verify(erfassungRepository).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_WithDelay() {
        // Arrange
        ErfassungDTO dto = new ErfassungDTO();
        dto.setStudentenId(1);
        dto.setStatusId(1);
        dto.setAnkunftszeit("08:15"); // 15 minutes late
        dto.setKommentar("Test Kommentar");

        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(statusRepository.findById(1)).thenReturn(Optional.of(testStatus));
        when(erfassungRepository.findByStudenten_IdAndDatum(1, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(erfassungRepository.saveAll(any())).thenReturn(Arrays.asList());

        // Act
        Integer result = erfassungService.erfassenAnwesenheiten(Arrays.asList(dto));

        // Assert
        assertEquals(1, result);
        verify(erfassungRepository).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_EmptyAnkunftszeit() {
        // Arrange
        ErfassungDTO dto = new ErfassungDTO();
        dto.setStudentenId(1);
        dto.setStatusId(1);
        dto.setAnkunftszeit(null); // No arrival time
        dto.setKommentar("Test Kommentar");

        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(statusRepository.findById(1)).thenReturn(Optional.of(testStatus));
        when(erfassungRepository.findByStudenten_IdAndDatum(1, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(erfassungRepository.saveAll(any())).thenReturn(Arrays.asList());

        // Act
        Integer result = erfassungService.erfassenAnwesenheiten(Arrays.asList(dto));

        // Assert
        assertEquals(1, result);
        verify(erfassungRepository).saveAll(any());
    }

    @Test
    public void testFindByGruppeUndMonat() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<Erfassung> expectedErfassungen = Arrays.asList(new Erfassung());

        when(erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(1, startDate, endDate))
                .thenReturn(expectedErfassungen);

        // Act
        List<Erfassung> result = erfassungService.findByGruppeUndMonat(1, startDate, endDate);

        // Assert
        assertEquals(expectedErfassungen, result);
        verify(erfassungRepository).findByStudenten_GruppeIdAndDatumBetween(1, startDate, endDate);
    }

    @Test
    public void testFindByStudentAndDate() {
        // Arrange
        LocalDate testDate = LocalDate.now();
        Erfassung expectedErfassung = new Erfassung();

        when(erfassungRepository.findByStudenten_IdAndDatum(1, testDate))
                .thenReturn(Optional.of(expectedErfassung));

        // Act
        Optional<Erfassung> result = erfassungService.findByStudentAndDate(1, testDate);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedErfassung, result.get());
        verify(erfassungRepository).findByStudenten_IdAndDatum(1, testDate);
    }

    @Test
    public void testSaveAll() {
        // Arrange
        List<Erfassung> erfassungen = Arrays.asList(new Erfassung(), new Erfassung());
        when(erfassungRepository.saveAll(erfassungen)).thenReturn(erfassungen);

        // Act
        erfassungService.saveAll(erfassungen);

        // Assert
        verify(erfassungRepository).saveAll(erfassungen);
    }
}
