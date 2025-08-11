--
-- Migration script to archive artefact data
--
-- Check DB/Server Name
DO
$ServerCheck$
  DECLARE
    correctServer varchar := 'flexible-pip-data-management-prod';
    correctDBName varchar := 'datamanagement';
    currentServer varchar;
    currentDatabase varchar;
  BEGIN
    SELECT replace(setting, '-data', ''), current_database()
    INTO currentServer, currentDatabase
    FROM pg_settings
    WHERE name = 'azure.customer_resource_group';

    IF currentServer <> correctServer THEN
      RAISE EXCEPTION
        USING DETAIL = 'Wrong SERVER! Server [' || currentServer || '] should be [' || correctServer || ']';
    END IF;

    IF currentDatabase <> correctDBName THEN
      RAISE EXCEPTION
        USING DETAIL = 'Wrong DB! DB [' || currentDatabase || '] should be [' || correctDBName || ']';
    END IF;

    RAISE NOTICE 'CORRECT Server and DB: Server [%], Database [%]', currentServer, currentDatabase;
  END
$ServerCheck$;

DO
$CR$
  DECLARE
    TicketNumber varchar(50) := 'DTSRD-xxxxx';
    AffectedRows integer;
    StatusText varchar(100);
    QueryExpectedResult int := 13;
    SecondQueryExpectedResult int := 13;
  BEGIN
    -- Step 1: Insert archived artefacts
    StatusText := 'Step 1';
    INSERT INTO public.artefact_archived (artefact_id, content_date, display_from, display_to, is_flat_file,
                                          "language", list_type, location_id, provenance, sensitivity, type,
                                          last_received_date, superseded_count, archived_date, is_manually_deleted)
    SELECT artefact_id, content_date, display_from, display_to, is_flat_file,
           "language", list_type, location_id, provenance, sensitivity, type,
           last_received_date, superseded_count, LOCALTIMESTAMP, TRUE
    FROM artefact
    WHERE is_archived = TRUE;

    GET DIAGNOSTICS AffectedRows = ROW_COUNT;
    IF AffectedRows <> QueryExpectedResult THEN
      RAISE EXCEPTION '% % : ERROR wrong number of rows added --> %', TicketNumber, StatusText, AffectedRows;
    END IF;

    RAISE NOTICE '% % : Rows INSERTED by query = %', TicketNumber, StatusText, AffectedRows;

    -- Step 2: Delete archived artefacts
    StatusText := 'Step 2';
    DELETE FROM artefact
    WHERE is_archived = TRUE;

    GET DIAGNOSTICS AffectedRows = ROW_COUNT;
    IF AffectedRows <> SecondQueryExpectedResult THEN
      RAISE EXCEPTION '% % : ERROR wrong number of rows deleted --> %', TicketNumber, StatusText, AffectedRows;
    END IF;

    RAISE NOTICE '% % : Rows DELETED by query = %', TicketNumber, StatusText, AffectedRows;

    RAISE NOTICE '% SUCCESSFUL COMPLETION, all changes COMMITTED.', TicketNumber;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE '% % : ERROR RAISED, all changes ROLLED BACK', TicketNumber, StatusText;
      RAISE EXCEPTION
        USING DETAIL = 'ERROR: Change Failed [' || SQLSTATE || '] ' || SQLERRM;
  END
$CR$;

/*
Output will be:
NOTICE:  CORRECT Server and DB : Server [flexible-pip-data-management-prod], Database [datamanagement]

NOTICE:  DTSRD-xxxxx Step 1 : Rows INSERT by query=13
NOTICE:  DTSRD-xxxxx Step 2 : Rows DELETED by query=13
NOTICE:  DTSRD-xxxxx SUCCESSFUL COMPLETION, all changes COMMITTED.
*/
