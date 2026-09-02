--
-- 1) Add new list type value CROWN_ADVANCE_PDDA_LIST to the check constraint
--
DO
$CR$
  DECLARE
    current_definition text;
    new_definition text;
  BEGIN
    SELECT pg_get_constraintdef(oid) INTO current_definition
    FROM pg_constraint
    WHERE conname = 'artefact_list_type_check';

    IF current_definition IS NULL THEN
      RAISE EXCEPTION 'Constraint artefact_list_type_check not found';
    END IF;

    new_definition := regexp_replace(
      current_definition,
      '\]\)::text\[\]',
      ', ''CROWN_ADVANCE_PDDA_LIST''::character varying])::text[]'
    );

    EXECUTE 'ALTER TABLE artefact DROP CONSTRAINT artefact_list_type_check';
    EXECUTE 'ALTER TABLE artefact ADD CONSTRAINT artefact_list_type_check ' || new_definition;
  END
$CR$;

--
-- 2) Duplicate CROWN_WARNED_PDDA_LIST artefact as CROWN_ADVANCE_PDDA_LIST
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
