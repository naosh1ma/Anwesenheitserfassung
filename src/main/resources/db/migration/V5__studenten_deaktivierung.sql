-- Studenten können deaktiviert werden, wenn sie die Gruppe verlassen.
-- Leer (NULL) bedeutet aktiv, sonst steht hier das Datum der Deaktivierung.
-- Deaktivierte Studenten erscheinen nicht mehr im Erfassungsformular, ihre Erfassungen bleiben erhalten.

ALTER TABLE studenten ADD COLUMN IF NOT EXISTS deaktiviert_am DATE;
