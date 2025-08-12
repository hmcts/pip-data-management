--
-- Delete all artefacts that are archived
--
DO
$CR$
  DECLARE
  BEGIN
    DELETE FROM artefact
    WHERE is_archived = TRUE;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'ERROR RAISED, all changes ROLLED BACK';
      RAISE EXCEPTION
        USING DETAIL = 'ERROR: Change Failed [' || SQLSTATE || '] ' || SQLERRM;
  END
$CR$;
