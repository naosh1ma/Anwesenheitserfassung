package com.art.erfassung.tests;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.StudentenRepository;
import com.art.erfassung.service.StudentenService;
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
 * Test class for {@link StudentenService}.
 */
public class StudentenServiceTest {

    @Mock
    private StudentenRepository studentenRepository;

    private StudentenService studentenService;

    private Gruppe testGruppe;
    private Studenten testStudent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        studentenService = new StudentenService(studentenRepository);

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
}
