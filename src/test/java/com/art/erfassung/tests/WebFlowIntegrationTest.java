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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests for the web layer.
 * These tests start the full application context (security, controllers and Thymeleaf templates)
 * against the H2 in-memory database of the test profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WebFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GruppeRepository gruppeRepository;

    @Autowired
    private StudentenRepository studentenRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private ErfassungRepository erfassungRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Gruppe gruppe;
    private Studenten student;
    private Status anwesend;
    private Status krankmeldung;

    @BeforeEach
    public void setup() {
        anwesend = statusRepository.save(new Status("Anwesend"));
        statusRepository.save(new Status("Entschuldigt"));
        statusRepository.save(new Status("Unentschuldigt"));
        krankmeldung = statusRepository.save(new Status("Krankmeldung"));
        gruppe = gruppeRepository.save(new Gruppe("Testgruppe"));
        student = studentenRepository.save(new Studenten("Mustermann", "Max", gruppe));
    }

    @AfterEach
    public void cleanup() {
        erfassungRepository.deleteAll();
        studentenRepository.deleteAll();
        gruppeRepository.deleteAll();
        statusRepository.deleteAll();
    }

    @Test
    public void testLoginPage_IsReachableAtLoginAndRoot() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    public void testGruppen_AccessibleForTeacher() throws Exception {
        mockMvc.perform(get("/gruppen").with(teacher()))
                .andExpect(status().isOk())
                .andExpect(view().name("gruppen"));
    }

    @Test
    public void testLogout_RedirectsToLoginPage() throws Exception {
        mockMvc.perform(logout()).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    public void testMissingResource_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/favicon.ico").with(teacher())).andExpect(status().isNotFound());
    }

    @Test
    public void testAnwesenheitForm_PreselectsAnwesend() throws Exception {
        // Act
        String html = render(get("/anwesenheit/{id}", gruppe.getId()));

        // Assert
        assertTrue(inputTag(html, "anwesend_0").contains("checked"));
        assertTrue(html.contains("Max Mustermann"));
    }

    @Test
    public void testSpeichern_PersistsAttendanceAndRedirects() throws Exception {
        // Act
        mockMvc.perform(post("/anwesenheit/{id}/speichern", gruppe.getId()).with(teacher()).with(csrf())
                        .param("eintraege[0].studentenId", String.valueOf(student.getId()))
                        .param("eintraege[0].statusId", String.valueOf(krankmeldung.getId()))
                        // Unfilled inputs are submitted as empty strings, like the browser does
                        .param("eintraege[0].ankunftszeit", "")
                        .param("eintraege[0].verlassenUm", "")
                        .param("eintraege[0].kommentar", ""))
                .andExpect(redirectedUrl("/anwesenheit/" + gruppe.getId()));

        // Assert
        Integer savedStatusId = transactionTemplate.execute(tx -> {
            List<Erfassung> erfassungen = erfassungRepository.findByStudenten_id(student.getId());
            assertEquals(1, erfassungen.size());
            return erfassungen.get(0).getStatus().getId();
        });
        assertEquals(krankmeldung.getId(), savedStatusId);
    }

    @Test
    public void testSpeichern_WithoutStatus_ShowsFormAgainWithMessage() throws Exception {
        // Act
        String html = mockMvc.perform(post("/anwesenheit/{id}/speichern", gruppe.getId()).with(teacher()).with(csrf())
                        .param("eintraege[0].studentenId", String.valueOf(student.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("anwesenheit"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        // Assert: the message is shown exactly once (layout and page must not both render it)
        assertEquals(1, html.split("Status-ID ist erforderlich", -1).length - 1);
        assertTrue(html.contains("Max Mustermann"));
        assertEquals(0, erfassungRepository.count());
    }

    @Test
    public void testSpeichern_InvalidStatusValue_IsNotRenderedAsHtml() throws Exception {
        // Act
        String html = mockMvc.perform(post("/anwesenheit/{id}/speichern", gruppe.getId()).with(teacher()).with(csrf())
                        .param("eintraege[0].studentenId", String.valueOf(student.getId()))
                        .param("eintraege[0].statusId", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        // Assert
        assertTrue(html.contains("Ungültiger Wert für eintraege[0].statusId"));
        assertFalse(html.contains("<script>alert(1)</script>"));
    }

    @Test
    public void testSpeichern_WithoutCsrfToken_IsForbidden() throws Exception {
        mockMvc.perform(post("/anwesenheit/{id}/speichern", gruppe.getId()).with(teacher())
                        .param("eintraege[0].studentenId", String.valueOf(student.getId()))
                        .param("eintraege[0].statusId", String.valueOf(anwesend.getId())))
                .andExpect(status().isForbidden());
        assertEquals(0, erfassungRepository.count());
    }

    @Test
    public void testSpeichern_StoresTimesAndPrefillsFormOnReopen() throws Exception {
        // Act: save with arrival and leave time
        mockMvc.perform(post("/anwesenheit/{id}/speichern", gruppe.getId()).with(teacher()).with(csrf())
                        .param("eintraege[0].studentenId", String.valueOf(student.getId()))
                        .param("eintraege[0].statusId", String.valueOf(anwesend.getId()))
                        .param("eintraege[0].ankunftszeit", "08:20")
                        .param("eintraege[0].verlassenUm", "15:00")
                        .param("eintraege[0].kommentar", "Arzttermin"))
                .andExpect(redirectedUrl("/anwesenheit/" + gruppe.getId()));

        // Assert: times are stored in their own columns and the comment is unchanged
        Erfassung gespeichert = transactionTemplate.execute(tx ->
                erfassungRepository.findByStudenten_id(student.getId()).get(0));
        assertEquals(LocalTime.of(8, 20), gespeichert.getAnkunftszeit());
        assertEquals(LocalTime.of(15, 0), gespeichert.getVerlassenUm());
        assertEquals("Arzttermin", gespeichert.getKommentar());

        // Assert: reopening the form shows the saved values, so saving again keeps them
        String html = render(get("/anwesenheit/{id}", gruppe.getId()));
        assertTrue(inputTag(html, "eintraege0.ankunftszeit").contains("value=\"08:20\""));
        assertTrue(inputTag(html, "eintraege0.verlassenUm").contains("value=\"15:00\""));
        assertTrue(inputTag(html, "eintraege0.kommentar").contains("value=\"Arzttermin\""));

        // Assert: arriving after 08:00 counts as late
        mockMvc.perform(get("/studenten/{id}", student.getId()).with(teacher()))
                .andExpect(model().attribute("statistik", hasProperty("verspaetungen", is(1L))));
    }

    @Test
    public void testSpeichern_StudentFromOtherGroup_IsRejected() throws Exception {
        // Arrange
        Gruppe andereGruppe = gruppeRepository.save(new Gruppe("Andere Gruppe"));
        Studenten fremderStudent = studentenRepository.save(new Studenten("Fremd", "Fritz", andereGruppe));

        // Act
        mockMvc.perform(post("/anwesenheit/{id}/speichern", gruppe.getId()).with(teacher()).with(csrf())
                        .param("eintraege[0].studentenId", String.valueOf(fremderStudent.getId()))
                        .param("eintraege[0].statusId", String.valueOf(anwesend.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("anwesenheit"))
                .andExpect(content().string(containsString("gehört nicht zu dieser Gruppe")));

        // Assert
        assertEquals(0, erfassungRepository.count());
    }

    @Test
    public void testStatistik_ShowsAttendanceAndKrankmeldungen() throws Exception {
        // Arrange
        erfassungRepository.save(new Erfassung(student, LocalDate.now().minusDays(1), anwesend, null));
        erfassungRepository.save(new Erfassung(student, LocalDate.now().minusDays(2), krankmeldung, null));

        // Act & Assert
        mockMvc.perform(get("/studenten/{id}", student.getId()).with(teacher()))
                .andExpect(status().isOk())
                .andExpect(view().name("statistik"))
                .andExpect(model().attribute("statistik", hasProperty("krank", is(1L))))
                .andExpect(model().attribute("statistik", hasProperty("gesamtAnwesenheit", is(50.0))))
                // German number format, independent of the browser language
                .andExpect(content().string(containsString("50,0%")));
    }

    @Test
    public void testAppPages_UseLayoutWithLogoutAndNoDuplicateHeader() throws Exception {
        // Arrange
        erfassungRepository.save(new Erfassung(student, LocalDate.now(), anwesend, null));
        String[] pages = {"/gruppen", "/anwesenheit/" + gruppe.getId(),
                "/liste/" + gruppe.getId(), "/studenten/" + student.getId()};

        // Act & Assert
        for (String page : pages) {
            String html = render(get(page));
            assertTrue(html.contains("action=\"/logout\""), page + " has no logout button");
            assertFalse(html.contains("class=\"page-header\""), page + " renders the layout header in addition to its own");
        }
    }

    @Test
    public void testNavigationLinks_AllResolve() throws Exception {
        // Arrange
        erfassungRepository.save(new Erfassung(student, LocalDate.now(), anwesend, null));
        String[] pages = {"/willkommen", "/gruppen", "/anwesenheit/" + gruppe.getId(),
                "/liste/" + gruppe.getId(), "/studenten/" + student.getId()};

        // Act: collect every internal link on the main pages
        Set<String> links = new LinkedHashSet<>();
        for (String page : pages) {
            Matcher matcher = Pattern.compile("href=\"(/[^\"]*)\"").matcher(render(get(page)));
            while (matcher.find()) {
                links.add(matcher.group(1));
            }
        }

        // Assert
        links.removeIf(link -> link.startsWith("/css/") || link.startsWith("/images/"));
        assertFalse(links.isEmpty());
        for (String link : links) {
            int status = mockMvc.perform(get(link).with(teacher())).andReturn().getResponse().getStatus();
            assertTrue(status < 400, link + " returned " + status);
        }
    }

    private static RequestPostProcessor teacher() {
        return user("teacher").roles("TEACHER");
    }

    private String render(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.with(teacher()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static String inputTag(String html, String id) {
        Matcher matcher = Pattern.compile("<input[^>]*\\bid=\"" + id + "\"[^>]*>").matcher(html);
        assertTrue(matcher.find(), "No input with id " + id);
        return matcher.group();
    }
}
