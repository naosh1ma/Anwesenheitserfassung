-- Status-Werte, auf deren Bezeichnungen sich die Anwendung verlässt
-- (Formular, Monatsübersicht und Statistik). Vorhandene Werte werden nicht doppelt angelegt.

INSERT INTO status (bezeichnung)
SELECT 'Anwesend' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM status WHERE bezeichnung = 'Anwesend');

INSERT INTO status (bezeichnung)
SELECT 'Entschuldigt' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM status WHERE bezeichnung = 'Entschuldigt');

INSERT INTO status (bezeichnung)
SELECT 'Unentschuldigt' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM status WHERE bezeichnung = 'Unentschuldigt');

INSERT INTO status (bezeichnung)
SELECT 'Krankmeldung' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM status WHERE bezeichnung = 'Krankmeldung');
