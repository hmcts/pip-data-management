CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE artefact
  DROP CONSTRAINT IF EXISTS duplicate_record_constraint;

ALTER TABLE artefact
  ADD CONSTRAINT duplicate_record_constraint
  EXCLUDE USING gist (location_id WITH =, content_date WITH =, language WITH =, list_type WITH =, provenance WITH =) WHERE (is_archived IS false);
