package com.art.erfassung.tests;

import com.art.erfassung.controller.ErfassungController;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.service.ErfassungService;
import com.art.erfassung.service.GruppeService;
import com.art.erfassung.service.StatusService;
import com.art.erfassung.service.StudentenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ErfassungController.class)
@Import(MockConfig.class)
public class ErfassungControllerIntegrationTest {
    //...
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentenService studentenService;

    @Autowired
    private ErfassungService erfassungService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private GruppeService gruppeService;

    @Test
    public void testGetStudentenListe() throws Exception {
        // Dummy-Daten vorbereiten
        Gruppe gruppe = new Gruppe();
        gruppe.setId(1);
        gruppe.setBezeichnung("Gruppe A");

        Studenten student1 = new Studenten();
        student1.setId(100);
        student1.setVorname("Max");
        student1.setName("Mustermann");
        student1.setGruppe(gruppe);

        Status status = new Status();
        status.setId(1);
        status.setBezeichnung("Anwesend");

        // Mocks konfigurieren
        when(gruppeService.findOrThrow(1)).thenReturn(gruppe);
        when(studentenService.findByGruppeId(1)).thenReturn(Arrays.asList(student1));
        when(statusService.findAll()).thenReturn(Arrays.asList(status));

        // GET-Anfrage ausführen und erwartete Werte prüfen
        mockMvc.perform(get("/anwesenheit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("studentenListe"))
                .andExpect(model().attributeExists("statusListe"))
                .andExpect(model().attributeExists("gruppe"))
                .andExpect(view().name("anwesenheit"));
    }
}
