--
-- Set artefact archive is_manually_deleted column default value to false
--
ALTER TABLE artefact_archived
  ALTER COLUMN is_manually_deleted SET DEFAULT FALSE;
