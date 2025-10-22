package com.art.erfassung.tests;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.model.Status;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.GruppeRepository;
import com.art.erfassung.repository.StatusRepository;
import com.art.erfassung.repository.StudentenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Erfassung application using JPA repositories.
 * These tests use the test profile with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
public class ErfassungApplicationIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GruppeRepository gruppeRepository;

    @Autowired
    private StudentenRepository studentenRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Test
    public void testGruppeRepository_SaveAndFind() {
        // Arrange
        Gruppe gruppe = new Gruppe("Test Gruppe Integration");

        // Act
        Gruppe savedGruppe = gruppeRepository.save(gruppe);
        entityManager.flush();
        entityManager.clear();

        Optional<Gruppe> foundGruppe = gruppeRepository.findById(savedGruppe.getId());

        // Assert
        assertTrue(foundGruppe.isPresent());
        assertEquals("Test Gruppe Integration", foundGruppe.get().getBezeichnung());
    }

    @Test
    public void testStudentenRepository_SaveAndFindByGruppe() {
        // Arrange
        Gruppe gruppe = new Gruppe("Test Gruppe");
        Gruppe savedGruppe = gruppeRepository.save(gruppe);
        entityManager.flush();

        Studenten student1 = new Studenten("Mustermann", "Max", savedGruppe);
        Studenten student2 = new Studenten("Musterfrau", "Anna", savedGruppe);

        // Act
        studentenRepository.save(student1);
        studentenRepository.save(student2);
        entityManager.flush();
        entityManager.clear();

        List<Studenten> students = studentenRepository.findByGruppeId(savedGruppe.getId());

        // Assert
        assertEquals(2, students.size());
        assertTrue(students.stream().anyMatch(s -> s.getName().equals("Mustermann")));
        assertTrue(students.stream().anyMatch(s -> s.getName().equals("Musterfrau")));
    }

    @Test
    public void testStatusRepository_SaveAndFindAll() {
        // Arrange
        Status status1 = new Status();
        status1.setBezeichnung("Anwesend");

        Status status2 = new Status();
        status2.setBezeichnung("Abwesend");

        // Act
        statusRepository.save(status1);
        statusRepository.save(status2);
        entityManager.flush();
        entityManager.clear();

        List<Status> statuses = statusRepository.findAll();

        // Assert
        assertEquals(2, statuses.size());
        assertTrue(statuses.stream().anyMatch(s -> s.getBezeichnung().equals("Anwesend")));
        assertTrue(statuses.stream().anyMatch(s -> s.getBezeichnung().equals("Abwesend")));
    }

    @Test
    public void testStudentenRepository_FindById() {
        // Arrange
        Gruppe gruppe = new Gruppe("Test Gruppe");
        Gruppe savedGruppe = gruppeRepository.save(gruppe);
        entityManager.flush();

        Studenten student = new Studenten("Test", "Student", savedGruppe);
        Studenten savedStudent = studentenRepository.save(student);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Studenten> foundStudent = studentenRepository.findById(savedStudent.getId());

        // Assert
        assertTrue(foundStudent.isPresent());
        assertEquals("Test", foundStudent.get().getName());
        assertEquals("Student", foundStudent.get().getVorname());
        assertEquals(savedGruppe.getId(), foundStudent.get().getGruppe().getId());
    }

    @Test
    public void testGruppeRepository_FindAll() {
        // Arrange
        Gruppe gruppe1 = new Gruppe("Gruppe 1");
        Gruppe gruppe2 = new Gruppe("Gruppe 2");

        gruppeRepository.save(gruppe1);
        gruppeRepository.save(gruppe2);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Gruppe> gruppen = gruppeRepository.findAll();

        // Assert
        assertEquals(2, gruppen.size());
        assertTrue(gruppen.stream().anyMatch(g -> g.getBezeichnung().equals("Gruppe 1")));
        assertTrue(gruppen.stream().anyMatch(g -> g.getBezeichnung().equals("Gruppe 2")));
    }
}
