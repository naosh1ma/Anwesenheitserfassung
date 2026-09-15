-- Lehrer sehen und erfassen nur die Gruppen, die ihnen zugewiesen sind. Administratoren sehen immer alle Gruppen.
-- Wird ein Benutzer oder eine Gruppe gelöscht, entfallen die zugehörigen Zuweisungen automatisch.

CREATE TABLE benutzer_gruppe (
    benutzer_id INT NOT NULL,
    gruppe_id   INT NOT NULL,
    PRIMARY KEY (benutzer_id, gruppe_id),
    CONSTRAINT fk_benutzer_gruppe_benutzer FOREIGN KEY (benutzer_id) REFERENCES benutzer (id) ON DELETE CASCADE,
    CONSTRAINT fk_benutzer_gruppe_gruppe FOREIGN KEY (gruppe_id) REFERENCES gruppe (id) ON DELETE CASCADE
);

-- Bisher haben alle Lehrer alle Gruppen gesehen. Damit sich für bestehende Konten nichts ändert,
-- erhalten sie alle vorhandenen Gruppen. Benutzer ohne Rolle gelten als Lehrer.
INSERT INTO benutzer_gruppe (benutzer_id, gruppe_id)
SELECT b.id, g.id
FROM benutzer b
CROSS JOIN gruppe g
WHERE b.rolle IS NULL OR b.rolle <> 'ADMIN';
