--
-- Deletes the `court_id` and `expiry_date` columns from the artefact table.
--
ALTER TABLE artefact
  DROP COLUMN IF EXISTS court_id,
  DROP COLUMN IF EXISTS expiry_date;
