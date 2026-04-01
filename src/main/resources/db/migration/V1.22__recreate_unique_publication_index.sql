ALTER TABLE artefact
  DROP CONSTRAINT IF EXISTS unique_publication_index,
  ADD CONSTRAINT unique_publication_index UNIQUE (location_id, content_date, language, list_type, provenance);
