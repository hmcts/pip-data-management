--
-- Duplicate CROWN_WARNED_PDDA_LIST artefact as CROWN_ADVANCE_PDDA_LIST
--
INSERT INTO artefact (
  artefact_id,
  content_date,
  display_from,
  display_to,
  is_flat_file,
  language,
  list_type,
  location_id,
  payload,
  provenance,
  search,
  sensitivity,
  source_artefact_id,
  type,
  last_received_date,
  superseded_count,
  payload_size
)
SELECT
  md5(random()::text || clock_timestamp()::text)::uuid AS artefact_id,
  content_date,
  display_from,
  display_to,
  is_flat_file,
  language,
  'CROWN_ADVANCE_PDDA_LIST',
  location_id,
  payload,
  provenance,
  search,
  sensitivity,
  source_artefact_id,
  type,
  last_received_date,
  superseded_count,
  payload_size
FROM artefact
WHERE list_type = 'CROWN_WARNED_PDDA_LIST';
