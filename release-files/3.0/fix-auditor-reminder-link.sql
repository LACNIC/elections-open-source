-- Correccion idempotente para templates generales y copias por eleccion.
-- Conserva el resto del contenido personalizado y no altera correos ya enviados.
BEGIN;
UPDATE electionemailtemplate
SET bodysp = replace(bodysp, '$auditor.resultLink,', '$auditor.resultLink'),
    bodyen = replace(bodyen, '$auditor.resultLink,', '$auditor.resultLink'),
    bodypt = replace(bodypt, '$auditor.resultLink,', '$auditor.resultLink')
WHERE type = 'AUDITOR_REMINDER_REVISION'
  AND (strpos(bodysp, '$auditor.resultLink,') > 0
    OR strpos(bodyen, '$auditor.resultLink,') > 0
    OR strpos(bodypt, '$auditor.resultLink,') > 0);
COMMIT;
