package com.art.erfassung.tests;

import com.art.erfassung.model.Benutzer;
import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Rolle;
import com.art.erfassung.repository.BenutzerRepository;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.service.BenutzerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for database-backed login and the user management page.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BenutzerVerwaltungIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BenutzerService benutzerService;

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GruppeRepository gruppeRepository;

    private Benutzer admin;
    private Benutzer lehrer;
    private final List<Integer> angelegteGruppen = new ArrayList<>();

    @BeforeEach
    public void setup() {
        admin = benutzerService.anlegen("admin", "Ada", "Admin", Rolle.ADMIN, "admin-passwort");
        lehrer = benutzerService.anlegen("lehrer", "Lena", "Lehrer", Rolle.TEACHER, "lehrer-passwort");
    }

    @AfterEach
    public void cleanup() {
        benutzerRepository.deleteAll();
        gruppeRepository.deleteAllById(angelegteGruppen);
    }

    @Test
    public void testBenutzerAnlegen_WithGroups_TeacherSeesOnlyThem() throws Exception {
        // Arrange
        Gruppe zugewiesen = gruppe("Zugewiesene Gruppe");
        gruppe("Andere Gruppe");

        // Act
        mockMvc.perform(post("/admin/benutzer").with(adminUser()).with(csrf())
                        .param("benutzername", "neu")
                        .param("vorname", "Nina")
                        .param("name", "Neu")
                        .param("rolle", "TEACHER")
                        .param("passwort", "geheim-123")
                        .param("gruppenIds", String.valueOf(zugewiesen.getId())))
                .andExpect(redirectedUrl("/admin/benutzer"));

        // Assert
        mockMvc.perform(get("/gruppen").with(user("neu").roles("TEACHER")))
                .andExpect(content().string(containsString("Zugewiesene Gruppe")))
                .andExpect(content().string(not(containsString("Andere Gruppe"))));
    }

    @Test
    public void testGruppenZuweisen_ReplacesAssignment() throws Exception {
        // Arrange
        Gruppe erste = gruppe("Erste Gruppe");
        Gruppe zweite = gruppe("Zweite Gruppe");

        // Act: assign both groups
        mockMvc.perform(post("/admin/benutzer/{id}/gruppen", lehrer.getId()).with(adminUser()).with(csrf())
                        .param("gruppenIds", String.valueOf(erste.getId()), String.valueOf(zweite.getId())))
                .andExpect(redirectedUrl("/admin/benutzer"))
                .andExpect(flash().attribute("successMessage", containsString("gespeichert")));

        // Assert: stored, and shown as checked on the assignment page
        assertEquals(2, gruppeRepository.findZugewieseneGruppen("lehrer").size());
        String html = mockMvc.perform(get("/admin/benutzer/{id}/gruppen", lehrer.getId()).with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-benutzer-gruppen"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("value=\"" + erste.getId() + "\" checked=\"checked\""));

        // Act & Assert: saving without a selection removes all assignments
        mockMvc.perform(post("/admin/benutzer/{id}/gruppen", lehrer.getId()).with(adminUser()).with(csrf()))
                .andExpect(redirectedUrl("/admin/benutzer"));
        assertTrue(gruppeRepository.findZugewieseneGruppen("lehrer").isEmpty());
    }

    @Test
    public void testGruppenZuweisen_ForAdmin_IsRejected() throws Exception {
        mockMvc.perform(post("/admin/benutzer/{id}/gruppen", admin.getId()).with(adminUser()).with(csrf()))
                .andExpect(redirectedUrl("/admin/benutzer/" + admin.getId() + "/gruppen"))
                .andExpect(flash().attribute("errorMessage", containsString("Administratoren")));
    }

    @Test
    public void testGruppeLoeschen_AssignedToTeacher_RemovesAssignment() throws Exception {
        // Arrange
        Gruppe leer = gruppe("Leere Gruppe");
        benutzerService.gruppenZuweisen(lehrer.getId(), List.of(leer.getId()));

        // Act
        mockMvc.perform(post("/admin/gruppen/{id}/loeschen", leer.getId()).with(adminUser()).with(csrf()))
                .andExpect(flash().attribute("successMessage", containsString("gelöscht")));

        // Assert
        assertTrue(gruppeRepository.findById(leer.getId()).isEmpty());
        assertTrue(gruppeRepository.findZugewieseneGruppen("lehrer").isEmpty());
    }

    private Gruppe gruppe(String bezeichnung) {
        Gruppe gruppe = gruppeRepository.save(new Gruppe(bezeichnung));
        angelegteGruppen.add(gruppe.getId());
        return gruppe;
    }

    @Test
    public void testFormLogin_DatabaseUserIsRedirectedToGruppen() throws Exception {
        mockMvc.perform(formLogin().user("lehrer").password("lehrer-passwort"))
                .andExpect(redirectedUrl("/gruppen"));
    }

    @Test
    public void testFormLogin_WrongPassword_IsRejected() throws Exception {
        mockMvc.perform(formLogin().user("lehrer").password("falsches-passwort"))
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    public void testFormLogin_OldHardcodedAccountsNoLongerWork() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("admin123"))
                .andExpect(redirectedUrl("/login?error=true"));
        mockMvc.perform(formLogin().user("teacher").password("teacher123"))
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    public void testBenutzerSeite_ForbiddenForTeacher() throws Exception {
        mockMvc.perform(get("/admin/benutzer").with(user("lehrer").roles("TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testBenutzerSeite_ListsUsersForAdmin() throws Exception {
        String html = mockMvc.perform(get("/admin/benutzer").with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("benutzer"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.contains("lehrer"));
        assertTrue(html.contains("href=\"/admin/benutzer\""), "admin menu link missing");
    }

    @Test
    public void testBenutzerAnlegen_NewUserCanLogIn() throws Exception {
        // Act
        mockMvc.perform(post("/admin/benutzer").with(adminUser()).with(csrf())
                        .param("benutzername", "neu")
                        .param("vorname", "Nina")
                        .param("name", "Neu")
                        .param("rolle", "TEACHER")
                        .param("passwort", "geheim-123"))
                .andExpect(redirectedUrl("/admin/benutzer"));

        // Assert: stored as hash, and the new account can log in
        String gespeichert = benutzerRepository.findByBenutzername("neu").orElseThrow().getPasswort();
        assertNotEquals("geheim-123", gespeichert);
        assertTrue(passwordEncoder.matches("geheim-123", gespeichert));
        mockMvc.perform(formLogin().user("neu").password("geheim-123"))
                .andExpect(redirectedUrl("/gruppen"));
    }

    @Test
    public void testBenutzerAnlegen_ShortPassword_ShowsError() throws Exception {
        mockMvc.perform(post("/admin/benutzer").with(adminUser()).with(csrf())
                        .param("benutzername", "neu")
                        .param("vorname", "Nina")
                        .param("name", "Neu")
                        .param("rolle", "TEACHER")
                        .param("passwort", "kurz"))
                .andExpect(status().isOk())
                .andExpect(view().name("benutzer"))
                .andExpect(content().string(containsString("mindestens 8 Zeichen")));

        assertTrue(benutzerRepository.findByBenutzername("neu").isEmpty());
    }

    @Test
    public void testBenutzerAnlegen_DuplicateUsername_ShowsError() throws Exception {
        mockMvc.perform(post("/admin/benutzer").with(adminUser()).with(csrf())
                        .param("benutzername", "lehrer")
                        .param("vorname", "Lars")
                        .param("name", "Lehrer")
                        .param("rolle", "TEACHER")
                        .param("passwort", "anderes-passwort"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bereits vergeben")));
    }

    @Test
    public void testPasswortSetzen_NewPasswordWorks() throws Exception {
        mockMvc.perform(post("/admin/benutzer/{id}/passwort", lehrer.getId()).with(adminUser()).with(csrf())
                        .param("passwort", "neues-passwort"))
                .andExpect(redirectedUrl("/admin/benutzer"))
                .andExpect(flash().attribute("successMessage", containsString("geändert")));

        mockMvc.perform(formLogin().user("lehrer").password("neues-passwort"))
                .andExpect(redirectedUrl("/gruppen"));
    }

    @Test
    public void testLoeschen_OwnAccount_IsRejected() throws Exception {
        mockMvc.perform(post("/admin/benutzer/{id}/loeschen", admin.getId()).with(adminUser()).with(csrf()))
                .andExpect(redirectedUrl("/admin/benutzer"))
                .andExpect(flash().attribute("errorMessage", containsString("eigenes Konto")));

        assertTrue(benutzerRepository.findByBenutzername("admin").isPresent());
    }

    @Test
    public void testLoeschen_OtherUser_IsDeleted() throws Exception {
        mockMvc.perform(post("/admin/benutzer/{id}/loeschen", lehrer.getId()).with(adminUser()).with(csrf()))
                .andExpect(redirectedUrl("/admin/benutzer"));

        assertTrue(benutzerRepository.findByBenutzername("lehrer").isEmpty());
    }

    private static RequestPostProcessor adminUser() {
        return user("admin").roles("ADMIN");
    }
}
