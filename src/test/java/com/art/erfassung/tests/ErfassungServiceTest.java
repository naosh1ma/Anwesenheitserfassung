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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private Studenten testStudent;
    private Status testStatus;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        erfassungService = new ErfassungService(erfassungRepository, statusRepository, studentenRepository);

        // Setup test data
        Gruppe testGruppe = new Gruppe("Test Gruppe");
        testGruppe.setId(1);

        testStudent = new Studenten("Mustermann", "Max", testGruppe);
        testStudent.setId(1);

        testStatus = new Status();
        testStatus.setId(1);
        testStatus.setBezeichnung("Anwesend");

        when(studentenRepository.findByGruppeId(1)).thenReturn(List.of(testStudent));
        when(statusRepository.findAll()).thenReturn(List.of(testStatus));
        when(erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(eq(1), any(), any())).thenReturn(List.of());
    }

    @Test
    public void testErfassenAnwesenheiten_NewErfassung_StoresTimesAndComment() {
        // Act
        erfassungService.erfassenAnwesenheiten(1, LocalDate.now(), List.of(dto(1, 1, "08:15", "16:30", "Test Kommentar")));

        // Assert
        List<Erfassung> gespeichert = captureSaved();
        assertEquals(1, gespeichert.size());
        Erfassung erfassung = gespeichert.get(0);
        assertEquals(testStudent, erfassung.getStudenten());
        assertEquals(testStatus, erfassung.getStatus());
        assertEquals(LocalDate.now(), erfassung.getDatum());
        assertEquals(LocalTime.of(8, 15), erfassung.getAnkunftszeit());
        assertEquals(LocalTime.of(16, 30), erfassung.getVerlassenUm());
        // Late arrivals are no longer written into the comment
        assertEquals("Test Kommentar", erfassung.getKommentar());
    }

    @Test
    public void testErfassenAnwesenheiten_UpdatesExistingErfassung() {
        // Arrange
        Erfassung vorhanden = new Erfassung(testStudent, LocalDate.now(), testStatus, "Alter Kommentar");
        vorhanden.setVerlassenUm(LocalTime.of(12, 0));
        when(erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(eq(1), any(), any()))
                .thenReturn(List.of(vorhanden));

        // Act
        erfassungService.erfassenAnwesenheiten(1, LocalDate.now(), List.of(dto(1, 1, "09:00", "", "Neuer Kommentar")));

        // Assert
        Erfassung erfassung = captureSaved().get(0);
        assertSame(vorhanden, erfassung);
        assertEquals("Neuer Kommentar", erfassung.getKommentar());
        assertEquals(LocalTime.of(9, 0), erfassung.getAnkunftszeit());
        assertNull(erfassung.getVerlassenUm());
    }

    @Test
    public void testErfassenAnwesenheiten_EmptyValues_StoredAsNull() {
        // Act
        erfassungService.erfassenAnwesenheiten(1, LocalDate.now(), List.of(dto(1, 1, "", null, "  ")));

        // Assert
        Erfassung erfassung = captureSaved().get(0);
        assertNull(erfassung.getAnkunftszeit());
        assertNull(erfassung.getVerlassenUm());
        assertNull(erfassung.getKommentar());
    }

    @Test
    public void testErfassenAnwesenheiten_StudentFromOtherGroup_Throws() {
        // Act & Assert
        IllegalArgumentException fehler = assertThrows(IllegalArgumentException.class,
                () -> erfassungService.erfassenAnwesenheiten(1, LocalDate.now(), List.of(dto(99, 1, "", "", ""))));
        assertTrue(fehler.getMessage().contains("gehört nicht zu dieser Gruppe"));
        verify(erfassungRepository, never()).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_LeaveBeforeArrival_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> erfassungService.erfassenAnwesenheiten(1, LocalDate.now(), List.of(dto(1, 1, "10:00", "09:00", ""))));
        verify(erfassungRepository, never()).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_InvalidEntry_ChangesNothing() {
        // Arrange: a valid entry for an existing record, followed by an invalid entry
        Erfassung vorhanden = new Erfassung(testStudent, LocalDate.now(), testStatus, "Alt");
        when(erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(eq(1), any(), any()))
                .thenReturn(List.of(vorhanden));

        // Act
        assertThrows(IllegalArgumentException.class, () -> erfassungService.erfassenAnwesenheiten(1, LocalDate.now(),
                List.of(dto(1, 1, "08:00", "", "Neu"), dto(99, 1, "", "", ""))));

        // Assert
        assertEquals("Alt", vorhanden.getKommentar());
        verify(erfassungRepository, never()).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_DeactivatedStudent_Throws() {
        // Arrange
        testStudent.setDeaktiviertAm(LocalDate.now().minusDays(1));

        // Act & Assert
        IllegalArgumentException fehler = assertThrows(IllegalArgumentException.class,
                () -> erfassungService.erfassenAnwesenheiten(1, LocalDate.now(), List.of(dto(1, 1, "", "", ""))));
        assertTrue(fehler.getMessage().contains("deaktiviert"));
        verify(erfassungRepository, never()).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_PastDay_StoresRecordForThatDay() {
        // Arrange
        LocalDate gestern = LocalDate.now().minusDays(1);

        // Act
        erfassungService.erfassenAnwesenheiten(1, gestern, List.of(dto(1, 1, "", "", "")));

        // Assert: existing records are looked up for that day and the new record gets that date
        assertEquals(gestern, captureSaved().get(0).getDatum());
        verify(erfassungRepository).findByStudenten_GruppeIdAndDatumBetween(1, gestern, gestern);
    }

    @Test
    public void testErfassenAnwesenheiten_FutureDay_Throws() {
        IllegalArgumentException fehler = assertThrows(IllegalArgumentException.class, () -> erfassungService
                .erfassenAnwesenheiten(1, LocalDate.now().plusDays(1), List.of(dto(1, 1, "", "", ""))));
        assertTrue(fehler.getMessage().contains("zukünftige"));
        verify(erfassungRepository, never()).saveAll(any());
    }

    @Test
    public void testErfassenAnwesenheiten_DayBeforeDeactivation_IsAllowed() {
        // Arrange: deactivated today, so yesterday the student was still active
        testStudent.setDeaktiviertAm(LocalDate.now());

        // Act
        erfassungService.erfassenAnwesenheiten(1, LocalDate.now().minusDays(1), List.of(dto(1, 1, "", "", "")));

        // Assert
        assertEquals(1, captureSaved().size());
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

    private static ErfassungDTO dto(int studentId, int statusId, String ankunftszeit, String verlassenUm, String kommentar) {
        ErfassungDTO dto = new ErfassungDTO();
        dto.setStudentenId(studentId);
        dto.setStatusId(statusId);
        dto.setAnkunftszeit(ankunftszeit);
        dto.setVerlassenUm(verlassenUm);
        dto.setKommentar(kommentar);
        return dto;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Erfassung> captureSaved() {
        ArgumentCaptor<List<Erfassung>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(erfassungRepository).saveAll(captor.capture());
        return captor.getValue();
    }
}
