package com.art.erfassung.tests;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.service.StatistikService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatistikServiceTest {

    @Mock
    private ErfassungRepository erfassungRepository;

    @InjectMocks
    private StatistikService statistikService;

    @Test
    public void testBerechneStudentenstatistik_MitDaten() {
        Studenten student = new Studenten();
        Status status = new Status();
        status.setBezeichnung("Anwesend");
        Erfassung erfassung = new Erfassung(student, LocalDate.now(), status, "Verspätung: 10 Minuten");
        when(erfassungRepository.findByStudenten_id(anyInt())).thenReturn(List.of(erfassung));
        List<Erfassung> erfassungen = Collections.singletonList(erfassung);
        when(erfassungRepository.findByStudenten_id(anyInt())).
                thenReturn(erfassungen);
        var statistik = statistikService.berechneStudentenstatistik(1);
        assertNotNull(statistik);
        assertEquals(100.0, statistik.gesamtAnwesenheit(), 0.01);
    }
}