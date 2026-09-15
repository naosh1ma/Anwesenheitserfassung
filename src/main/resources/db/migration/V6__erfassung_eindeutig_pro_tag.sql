-- Höchstens eine Erfassung pro Student und Tag.
-- Enthält die Datenbank bereits doppelte Erfassungen, bricht diese Migration ab und die Anwendung startet nicht.
-- Es wird nichts automatisch gelöscht; siehe README, Abschnitt „Doppelte Erfassungen“.

ALTER TABLE erfassung ADD CONSTRAINT uk_erfassung_student_datum UNIQUE (studenten_id, datum);
