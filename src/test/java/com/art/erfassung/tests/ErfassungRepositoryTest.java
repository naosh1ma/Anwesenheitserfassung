package com.art.erfassung.tests;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ErfassungRepositoryTest {

    @Autowired
    private ErfassungRepository erfassungRepository;

    @Test
    public void testFindByStudenten_id() {
        // Erstelle und persistiere ggf. einen Dummy-Studenten und zugehörige Erfassung
        Studenten studenten = new Studenten();
        // Hier musst du die Felder des Studenten setzen und den Studenten persistieren, z.B. mit TestEntityManager.
        // Für dieses Beispiel gehen wir davon aus, dass es bereits in der Test-Datenbank existiert.

        // Erstelle eine Erfassung für diesen Studenten
        Erfassung erfassung = new Erfassung(studenten, LocalDate.now(), /*dummy Status*/ null, "Kommentar");
        erfassungRepository.save(erfassung);

        // Führe die Methode aus
        List<Erfassung> result = erfassungRepository.findByStudenten_id(studenten.getId());
        // Überprüfe, ob die Erfassung gefunden wird
        assertThat(result).isNotEmpty();
    }
}
