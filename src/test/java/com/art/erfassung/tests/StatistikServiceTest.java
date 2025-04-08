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
    public void testBerechneFuerStudent_MitDaten() {
        // Erstelle Dummy-Objekte für Studenten und Status
        Studenten student = new Studenten();
        // Hier solltest du gegebenenfalls Setter oder einen Konstruktor verwenden, um relevante Felder zu befüllen.
        Status statusAnwesend = new Status();
        // Setze hier beispielhaft die Bezeichnung (z. B. "Anwesend")
        // Annahme: Es gibt eine Methode setBezeichnung() oder einen Konstruktor.
        statusAnwesend.setBezeichnung("Anwesend");
        // Erstelle eine Erfassungsliste
        Erfassung erfassung1 = new Erfassung(student, LocalDate.now(), statusAnwesend, "");
        List<Erfassung> erfassungen = Collections.singletonList(erfassung1);
        // Wenn der Repository-Aufruf getriggert wird, gib die erfassungen zurück
        when(erfassungRepository.findByStudenten_id(anyInt())).thenReturn(erfassungen);
        // Aufruf der Methode, die getestet werden soll
        var statistik = statistikService.berechneFuerStudent(1);
        // Überprüfe das Ergebnis
        assertNotNull(statistik);
        // Beispiel: Da ein Eintrag "Anwesend" vorhanden ist, sollte die Anwesenheitsquote 100% sein.
        assertEquals(100.0, statistik.gesamtAnwesenheit(), 0.01);
    }
}
