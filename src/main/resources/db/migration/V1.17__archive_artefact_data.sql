--
-- Move archived artefact data to the archived artefact table
--
DO
$CR$
  DECLARE
  BEGIN
    INSERT INTO public.artefact_archived (artefact_id, content_date, display_from, display_to, is_flat_file,
                                          "language", list_type, location_id, provenance, sensitivity, type,
                                          last_received_date, superseded_count, archived_date, is_manually_deleted)
    SELECT a.artefact_id, a.content_date, a.display_from, a.display_to, a.is_flat_file,
           a."language", a.list_type, a.location_id, a.provenance, a.sensitivity, a.type,
           a.last_received_date, a.superseded_count, LOCALTIMESTAMP, TRUE
    FROM artefact a
    LEFT JOIN public.artefact_archived aa
      on a.artefact_id = aa.artefact_id
    WHERE a.is_archived = TRUE
    AND aa.artefact_id IS NULL;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'ERROR RAISED, all changes ROLLED BACK';
      RAISE EXCEPTION
        USING DETAIL = 'ERROR: Change Failed [' || SQLSTATE || '] ' || SQLERRM;
  END
$CR$;
