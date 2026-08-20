-- 1) Duplicate in artefact
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
  a.content_date,
  a.display_from,
  a.display_to,
  a.is_flat_file,
  a.language,
  'CROWN_ADVANCE_PDDA_LIST',
  a.location_id,
  a.payload,
  a.provenance,
  a.search,
  a.sensitivity,
  a.source_artefact_id,
  a.type,
  a.last_received_date,
  a.superseded_count,
  a.payload_size
FROM artefact a
WHERE a.list_type = 'CROWN_WARNED_PDDA_LIST'
  AND NOT EXISTS (
  SELECT 1
  FROM artefact x
  WHERE x.location_id IS NOT DISTINCT FROM a.location_id
    AND x.content_date IS NOT DISTINCT FROM a.content_date
    AND x.language IS NOT DISTINCT FROM a.language
    AND x.provenance IS NOT DISTINCT FROM a.provenance
    AND x.list_type = 'CROWN_ADVANCE_PDDA_LIST'
);

-- -- 2) Duplicate in artefact_archived
INSERT INTO artefact_archived (
  artefact_id,
  content_date,
  display_from,
  display_to,
  is_flat_file,
  language,
  list_type,
  location_id,
  provenance,
  sensitivity,
  type,
  last_received_date,
  superseded_count,
  archived_date,
  is_manually_deleted
)
SELECT
  md5(random()::text || clock_timestamp()::text)::uuid AS artefact_id,
  aa.content_date,
  aa.display_from,
  aa.display_to,
  aa.is_flat_file,
  aa.language,
  'CROWN_ADVANCE_PDDA_LIST',
  aa.location_id,
  aa.provenance,
  aa.sensitivity,
  aa.type,
  aa.last_received_date,
  aa.superseded_count,
  aa.archived_date,
  aa.is_manually_deleted
FROM artefact_archived aa
WHERE aa.list_type = 'CROWN_WARNED_PDDA_LIST'
  AND NOT EXISTS (
  SELECT 1
  FROM artefact_archived y
  WHERE y.location_id IS NOT DISTINCT FROM aa.location_id
    AND y.content_date IS NOT DISTINCT FROM aa.content_date
    AND y.language IS NOT DISTINCT FROM aa.language
    AND y.provenance IS NOT DISTINCT FROM aa.provenance
    AND y.list_type = 'CROWN_ADVANCE_PDDA_LIST'
);
