package com.art.erfassung.tests;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.repository.StudentenRepository;
import com.art.erfassung.service.StudentenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link StudentenService}.
 */
public class StudentenServiceTest {

    @Mock
    private StudentenRepository studentenRepository;

    @Mock
    private GruppeRepository gruppeRepository;

    @Mock
    private ErfassungRepository erfassungRepository;

    private StudentenService studentenService;

    private Gruppe testGruppe;
    private Studenten testStudent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        studentenService = new StudentenService(studentenRepository, gruppeRepository, erfassungRepository);

        // Setup test data
        testGruppe = new Gruppe("Test Gruppe");
        testGruppe.setId(1);

        testStudent = new Studenten("Mustermann", "Max", testGruppe);
        testStudent.setId(1);
    }

    @Test
    public void testFindByGruppeId() {
        // Arrange
        Studenten student2 = new Studenten("Musterfrau", "Anna", testGruppe);
        student2.setId(2);
        List<Studenten> expectedStudents = Arrays.asList(testStudent, student2);

        when(studentenRepository.findByGruppeId(1)).thenReturn(expectedStudents);

        // Act
        List<Studenten> actualStudents = studentenService.findByGruppeId(1);

        // Assert
        assertEquals(expectedStudents, actualStudents);
        verify(studentenRepository, times(1)).findByGruppeId(1);
    }

    @Test
    public void testFindOrThrow_ExistingId() {
        // Arrange
        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));

        // Act
        Studenten result = studentenService.findOrThrow(1);

        // Assert
        assertEquals(testStudent, result);
        verify(studentenRepository, times(1)).findById(1);
    }

    @Test
    public void testFindOrThrow_NonExistingId() {
        // Arrange
        when(studentenRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            studentenService.findOrThrow(999);
        });
        verify(studentenRepository, times(1)).findById(999);
    }

    @Test
    public void testFindByGruppeId_EmptyResult() {
        // Arrange
        when(studentenRepository.findByGruppeId(999)).thenReturn(Arrays.asList());

        // Act
        List<Studenten> actualStudents = studentenService.findByGruppeId(999);

        // Assert
        assertTrue(actualStudents.isEmpty());
        verify(studentenRepository, times(1)).findByGruppeId(999);
    }

    @Test
    public void testFindAktiveByGruppeId_SortedByName() {
        // Arrange
        Studenten anna = new Studenten("Musterfrau", "Anna", testGruppe);
        when(studentenRepository.findByGruppeIdAndDeaktiviertAmIsNull(1)).thenReturn(List.of(testStudent, anna));

        // Act & Assert
        assertEquals(List.of(anna, testStudent), studentenService.findAktiveByGruppeId(1));
    }

    @Test
    public void testFindAlleByGruppeIdSortiert_ActiveFirst() {
        // Arrange
        Studenten deaktiviert = new Studenten("Adler", "Albert", testGruppe);
        deaktiviert.setDeaktiviertAm(LocalDate.now());
        when(studentenRepository.findByGruppeId(1)).thenReturn(List.of(deaktiviert, testStudent));

        // Act & Assert
        assertEquals(List.of(testStudent, deaktiviert), studentenService.findAlleByGruppeIdSortiert(1));
    }

    @Test
    public void testAnlegen_SavesActiveStudentInGroup() {
        // Arrange
        when(gruppeRepository.findById(1)).thenReturn(Optional.of(testGruppe));
        when(studentenRepository.save(any(Studenten.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Studenten student = studentenService.anlegen(1, " Anna ", " Schmidt ");

        // Assert
        assertEquals("Anna", student.getVorname());
        assertEquals("Schmidt", student.getName());
        assertEquals(testGruppe, student.getGruppe());
        assertTrue(student.isAktiv());
    }

    @Test
    public void testAnlegen_BlankFirstName_Throws() {
        assertThrows(IllegalArgumentException.class, () -> studentenService.anlegen(1, "  ", "Schmidt"));
        verify(studentenRepository, never()).save(any());
    }

    @Test
    public void testDeaktivieren_SetsToday() {
        // Arrange
        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));

        // Act
        studentenService.deaktivieren(1);

        // Assert
        assertEquals(LocalDate.now(), testStudent.getDeaktiviertAm());
        assertFalse(testStudent.isAktiv());
        verify(studentenRepository).save(testStudent);
    }

    @Test
    public void testReaktivieren_ClearsDate() {
        // Arrange
        testStudent.setDeaktiviertAm(LocalDate.of(2026, 9, 1));
        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studentenRepository.save(any(Studenten.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        studentenService.reaktivieren(1);

        // Assert
        assertTrue(testStudent.isAktiv());
    }

    @Test
    public void testLoeschen_WithRecords_Throws() {
        // Arrange
        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(erfassungRepository.findByStudenten_id(1)).thenReturn(List.of(new Erfassung()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> studentenService.loeschen(1));
        verify(studentenRepository, never()).delete(any());
    }

    @Test
    public void testLoeschen_WithoutRecords_Deletes() {
        // Arrange
        when(studentenRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(erfassungRepository.findByStudenten_id(1)).thenReturn(List.of());

        // Act
        studentenService.loeschen(1);

        // Assert
        verify(studentenRepository).delete(testStudent);
    }
}
