package com.art.erfassung.tests;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.repository.StatusRepository;
import com.art.erfassung.repository.StudentenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for managing groups and students, including deactivated students.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GruppenVerwaltungIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GruppeRepository gruppeRepository;

    @Autowired
    private StudentenRepository studentenRepository;

    @Autowired
    private ErfassungRepository erfassungRepository;

    @Autowired
    private StatusRepository statusRepository;

    private Gruppe gruppe;
    private Studenten student;

    @BeforeEach
    public void setup() {
        gruppe = gruppeRepository.save(new Gruppe("Informatik 1A"));
        student = studentenRepository.save(new Studenten("Mustermann", "Max", gruppe));
    }

    @AfterEach
    public void cleanup() {
        erfassungRepository.deleteAll();
        studentenRepository.deleteAll();
        gruppeRepository.deleteAll();
    }

    @Test
    public void testGruppenVerwaltung_ForbiddenForTeacher() throws Exception {
        mockMvc.perform(get("/admin/gruppen").with(teacher())).andExpect(status().isForbidden());
    }

    @Test
    public void testGruppeAnlegen_AppearsInList() throws Exception {
        // Act
        mockMvc.perform(post("/admin/gruppen").with(admin()).with(csrf()).param("bezeichnung", "Mathematik 2B"))
                .andExpect(redirectedUrl("/admin/gruppen"))
                .andExpect(flash().attribute("successMessage", containsString("angelegt")));

        // Assert
        assertTrue(gruppeRepository.findAll().stream().anyMatch(g -> "Mathematik 2B".equals(g.getBezeichnung())));
        mockMvc.perform(get("/admin/gruppen").with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mathematik 2B")));
    }

    @Test
    public void testGruppeAnlegen_DuplicateName_ShowsError() throws Exception {
        mockMvc.perform(post("/admin/gruppen").with(admin()).with(csrf()).param("bezeichnung", "informatik 1a"))
                .andExpect(flash().attribute("errorMessage", containsString("gibt es bereits")));

        assertEquals(1, gruppeRepository.count());
    }

    @Test
    public void testGruppeUmbenennen() throws Exception {
        mockMvc.perform(post("/admin/gruppen/{id}/umbenennen", gruppe.getId()).with(admin()).with(csrf())
                        .param("bezeichnung", "Informatik 1B"))
                .andExpect(flash().attribute("successMessage", containsString("umbenannt")));

        assertEquals("Informatik 1B", gruppeRepository.findById(gruppe.getId()).orElseThrow().getBezeichnung());
    }

    @Test
    public void testGruppeLoeschen_WithStudents_IsRejected() throws Exception {
        mockMvc.perform(post("/admin/gruppen/{id}/loeschen", gruppe.getId()).with(admin()).with(csrf()))
                .andExpect(flash().attribute("errorMessage", containsString("enthält noch Studenten")));

        assertTrue(gruppeRepository.findById(gruppe.getId()).isPresent());
    }

    @Test
    public void testGruppeLoeschen_Empty_IsDeleted() throws Exception {
        Gruppe leer = gruppeRepository.save(new Gruppe("Leere Gruppe"));

        mockMvc.perform(post("/admin/gruppen/{id}/loeschen", leer.getId()).with(admin()).with(csrf()))
                .andExpect(flash().attribute("successMessage", containsString("gelöscht")));

        assertTrue(gruppeRepository.findById(leer.getId()).isEmpty());
    }

    @Test
    public void testStudentAnlegenUndVerschieben() throws Exception {
        // Act: add a student
        mockMvc.perform(post("/admin/gruppen/{id}/studenten", gruppe.getId()).with(admin()).with(csrf())
                        .param("vorname", "Anna").param("name", "Schmidt"))
                .andExpect(redirectedUrl("/admin/gruppen/" + gruppe.getId()))
                .andExpect(flash().attribute("successMessage", containsString("hinzugefügt")));
        Studenten anna = studentenRepository.findByGruppeId(gruppe.getId()).stream()
                .filter(s -> s.getVorname().equals("Anna")).findFirst().orElseThrow();
        mockMvc.perform(get("/admin/gruppen/{id}", gruppe.getId()).with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Schmidt\"")));

        // Act: rename and move to another group
        Gruppe andereGruppe = gruppeRepository.save(new Gruppe("Mathematik 2B"));
        mockMvc.perform(post("/admin/gruppen/{id}/studenten/{sid}", gruppe.getId(), anna.getId()).with(admin()).with(csrf())
                        .param("vorname", "Anna").param("name", "Schmidt-Meyer")
                        .param("gruppeId", String.valueOf(andereGruppe.getId())))
                .andExpect(flash().attribute("successMessage", containsString("verschoben")));

        // Assert
        Studenten verschoben = studentenRepository.findById(anna.getId()).orElseThrow();
        assertEquals("Schmidt-Meyer", verschoben.getName());
        assertEquals(andereGruppe.getId(), verschoben.getGruppe().getId());
    }

    @Test
    public void testStudentAnlegen_BlankName_ShowsError() throws Exception {
        mockMvc.perform(post("/admin/gruppen/{id}/studenten", gruppe.getId()).with(admin()).with(csrf())
                        .param("vorname", "  ").param("name", "Schmidt"))
                .andExpect(flash().attribute("errorMessage", containsString("Vornamen")));

        assertEquals(1, studentenRepository.findByGruppeId(gruppe.getId()).size());
    }

    @Test
    public void testStudentDeaktivieren_HiddenFromFormButKeptInHistory() throws Exception {
        // Arrange: the student already has an attendance record this month
        Status anwesend = statusRepository.findByBezeichnung("Anwesend").orElseThrow();
        erfassungRepository.save(new Erfassung(student, LocalDate.now(), anwesend, null));

        // Act: deactivate
        mockMvc.perform(post("/admin/gruppen/{id}/studenten/{sid}/deaktivieren", gruppe.getId(), student.getId())
                        .with(admin()).with(csrf()))
                .andExpect(flash().attribute("successMessage", containsString("deaktiviert")));
        assertEquals(LocalDate.now(), studentenRepository.findById(student.getId()).orElseThrow().getDeaktiviertAm());

        // Assert: no longer in the attendance form
        mockMvc.perform(get("/anwesenheit/{id}", gruppe.getId()).with(teacher()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Max Mustermann"))));

        // Assert: still in the monthly list with the record, marked as deactivated
        mockMvc.perform(get("/liste/{id}", gruppe.getId()).with(teacher()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Max Mustermann")))
                .andExpect(content().string(containsString("class=\"inaktiv-hinweis\">deaktiviert<")));

        // Assert: attendance can no longer be saved for the student
        mockMvc.perform(post("/anwesenheit/{id}/speichern", gruppe.getId()).with(teacher()).with(csrf())
                        .param("eintraege[0].studentenId", String.valueOf(student.getId()))
                        .param("eintraege[0].statusId", String.valueOf(anwesend.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ist deaktiviert")));

        // Act & Assert: reactivating brings the student back into the form
        mockMvc.perform(post("/admin/gruppen/{id}/studenten/{sid}/reaktivieren", gruppe.getId(), student.getId())
                        .with(admin()).with(csrf()))
                .andExpect(flash().attribute("successMessage", containsString("reaktiviert")));
        mockMvc.perform(get("/anwesenheit/{id}", gruppe.getId()).with(teacher()))
                .andExpect(content().string(containsString("Max Mustermann")));
    }

    @Test
    public void testStudentLoeschen_WithRecords_IsRejected() throws Exception {
        Status anwesend = statusRepository.findByBezeichnung("Anwesend").orElseThrow();
        erfassungRepository.save(new Erfassung(student, LocalDate.now(), anwesend, null));

        mockMvc.perform(post("/admin/gruppen/{id}/studenten/{sid}/loeschen", gruppe.getId(), student.getId())
                        .with(admin()).with(csrf()))
                .andExpect(flash().attribute("errorMessage", containsString("Deaktivieren Sie")));

        assertTrue(studentenRepository.findById(student.getId()).isPresent());
    }

    @Test
    public void testStudentLoeschen_WithoutRecords_IsDeleted() throws Exception {
        mockMvc.perform(post("/admin/gruppen/{id}/studenten/{sid}/loeschen", gruppe.getId(), student.getId())
                        .with(admin()).with(csrf()))
                .andExpect(flash().attribute("successMessage", containsString("gelöscht")));

        assertTrue(studentenRepository.findById(student.getId()).isEmpty());
    }

    @Test
    public void testMonatsliste_EmptyMonthParameter_ShowsCurrentMonth() throws Exception {
        mockMvc.perform(get("/liste/{id}", gruppe.getId()).param("monat", "").with(teacher()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("monat", LocalDate.now().toString().substring(0, 7)));
    }

    private static RequestPostProcessor admin() {
        return user("admin").roles("ADMIN");
    }

    private static RequestPostProcessor teacher() {
        return user("teacher").roles("TEACHER");
    }
}
