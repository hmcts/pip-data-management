--
-- This is used to prevent multiple active records with the same attributes from being created during concurrent
-- publication uploads before the first record is saved.
--
CREATE UNIQUE INDEX IF NOT EXISTS unique_publication_index
  ON artefact(location_id, content_date, language, list_type, provenance)
  WHERE is_archived IS false;
