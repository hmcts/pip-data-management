CREATE UNIQUE INDEX IF NOT EXISTS unique_record_index
  ON artefact(location_id, content_date, language, list_type, provenance)
  WHERE is_archived IS false;
