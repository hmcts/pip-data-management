--
-- Delete all artefacts that are archived
--
DO
$CR$
  DECLARE
  BEGIN
    DELETE FROM artefact a
      USING artefact_archived aa
    WHERE a.artefact_id = aa.artefact_id
      AND a.is_archived = TRUE;

    RAISE NOTICE 'All archived artefacts have been deleted successfully.';

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'ERROR RAISED, all changes ROLLED BACK';
      RAISE EXCEPTION
        USING DETAIL = 'ERROR: Change Failed [' || SQLSTATE || '] ' || SQLERRM;
  END
$CR$;
