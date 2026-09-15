package com.art.erfassung.tests;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.service.StatistikService;
import com.art.erfassung.service.StatistikService.StatistikErgebnis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link StatistikService}.
 */
public class StatistikServiceTest {

    @Mock
    private ErfassungRepository erfassungRepository;

    private Studenten student;
    private Status anwesend;
    private Status krankmeldung;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        student = new Studenten("Mustermann", "Max", new Gruppe("Test Gruppe"));
        student.setId(1);
        anwesend = new Status("Anwesend");
        krankmeldung = new Status("Krankmeldung");
    }

    @Test
    public void testBerechneStudentenstatistik_CountsLateArrivalsFromStoredTimes() {
        // Arrange
        when(erfassungRepository.findByStudenten_id(1)).thenReturn(List.of(
                erfassung(anwesend, LocalTime.of(8, 15), null),
                erfassung(anwesend, LocalTime.of(7, 55), null),
                erfassung(anwesend, LocalTime.of(8, 0), null),
                erfassung(krankmeldung, null, null)));

        // Act
        StatistikErgebnis ergebnis = new StatistikService(erfassungRepository, "08:00").berechneStudentenstatistik(1);

        // Assert: only the arrival after 08:00 is late
        assertEquals(1, ergebnis.verspaetungen());
        assertEquals(1, ergebnis.krank());
        assertEquals(75.0, ergebnis.gesamtAnwesenheit());
    }

    @Test
    public void testBerechneStudentenstatistik_LegacyCommentWithoutTime_CountsAsLate() {
        // Arrange: older records stored lateness only in the comment
        when(erfassungRepository.findByStudenten_id(1)).thenReturn(List.of(
                erfassung(anwesend, null, "Verspätung: 10 Minuten"),
                erfassung(anwesend, LocalTime.of(7, 50), "Verspätung: 5 Minuten"),
                erfassung(anwesend, null, null)));

        // Act
        StatistikErgebnis ergebnis = new StatistikService(erfassungRepository, "08:00").berechneStudentenstatistik(1);

        // Assert: the comment only counts when no arrival time is stored
        assertEquals(1, ergebnis.verspaetungen());
    }

    @Test
    public void testBerechneStudentenstatistik_UsesConfiguredStartTime() {
        // Arrange
        when(erfassungRepository.findByStudenten_id(1)).thenReturn(List.of(
                erfassung(anwesend, LocalTime.of(8, 15), null),
                erfassung(anwesend, LocalTime.of(8, 45), null)));

        // Act
        StatistikErgebnis ergebnis = new StatistikService(erfassungRepository, "08:30").berechneStudentenstatistik(1);

        // Assert
        assertEquals(1, ergebnis.verspaetungen());
    }

    private Erfassung erfassung(Status status, LocalTime ankunftszeit, String kommentar) {
        Erfassung erfassung = new Erfassung(student, LocalDate.of(2026, 9, 1), status, kommentar);
        erfassung.setAnkunftszeit(ankunftszeit);
        return erfassung;
    }
}
