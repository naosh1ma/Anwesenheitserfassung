-- Ankunfts- und Verlassen-Zeit je Erfassung; Kommentare bis 500 Zeichen (wie im Formular erlaubt).
-- Die Spalten können im dev-Profil bereits automatisch angelegt worden sein.

ALTER TABLE erfassung ADD COLUMN IF NOT EXISTS ankunftszeit TIME;
ALTER TABLE erfassung ADD COLUMN IF NOT EXISTS verlassen_um TIME;
ALTER TABLE erfassung MODIFY kommentar VARCHAR(500);
