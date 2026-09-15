-- Rolle der Benutzer und ausreichend Platz für BCrypt-Hashes (60 Zeichen).
-- Die Anweisungen funktionieren auch, wenn die Spalte schon existiert: Frühere Versionen haben sie
-- im dev-Profil automatisch angelegt, auf MariaDB als ENUM-Spalte. Sie wird hier in VARCHAR umgewandelt.

ALTER TABLE benutzer ADD COLUMN IF NOT EXISTS rolle VARCHAR(20);
ALTER TABLE benutzer MODIFY rolle VARCHAR(20);
ALTER TABLE benutzer MODIFY passwort VARCHAR(255);
