--
-- Deletes the `is_archived` column from the artefact table.
--
ALTER TABLE artefact
  DROP COLUMN IF EXISTS is_archived,
