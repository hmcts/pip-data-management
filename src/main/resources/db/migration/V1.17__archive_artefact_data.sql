--
-- Migration script to archive artefact data
--
-- Check DB/Server Name
DO
$ServerCheck$
	DECLARE
correctServer varchar = 'flexible-pip-data-management-prod';
		correctDBName varchar = 'datamanagement';
		currentServer varchar;
		currentDatabase varchar;
BEGIN

select replace(setting,'-data',''), current_database()
into currentServer,currentDatabase
from pg_settings
where name='azure.customer_resource_group';

if currentServer <> correctServer THEN
	    RAISE EXCEPTION
	      USING DETAIL = 'Wrong SERVER! Server [' || currentServer|| '] should be [' || correctServer|| ']';
END IF;
        if currentDatabase <> correctDBName THEN
	    RAISE EXCEPTION
	      USING DETAIL = 'Wrong DB! DB [' || currentDatabase || '] should be [' || correctDBName|| ']';
END IF;
		RAISE NOTICE E'CORRECT Server and DB : Server [%], Database [%]\n',currentServer,currentDatabase;
END
$ServerCheck$;

--DTSRD-xxxxx - move archive artefacts to artefact_archived table
DO
$CR$
DECLARE
TicketNumber varchar(10) := 'DTSRD-xxxxx';
  AffectedRows integer;
  StatusText varchar(20);
  QueryExpectedResult int := 6;

BEGIN
    -- Step 1
    StatusText='Step 1';
INSERT INTO artefact_archived (artefact_id, content_date, display_from, display_to, is_flat_file,
                               "language", list_type,location_id,  provenance, sensitivity, type,
                               last_received_date, superseded_count, archived_date, is_manually_deleted)
SELECT artefact_id, content_date, display_from, display_to, is_flat_file,
       "language", list_type,location_id, provenance, sensitivity, type,
       last_received_date, superseded_count, LOCALTIMESTAMP, TRUE
FROM artefact
WHERE is_archived = TRUE;

GET DIAGNOSTICS AffectedRows = ROW_COUNT;
if AffectedRows <> QueryExpectedResult then
       RAISE EXCEPTION '% % : ERROR wrong number of rows added --> %',TicketNumber,StatusText,AffectedRows;
end if;
    RAISE NOTICE '% % : Rows INSERT by query=%', TicketNumber,StatusText, AffectedRows ;

	RAISE NOTICE E'% SUCCESSFUL COMPLETION, all changes COMMITTED.\n', TicketNumber;

EXCEPTION
    WHEN others THEN
    RAISE NOTICE '% % : ERROR RAISED, all changes ROLLED BACK', TicketNumber, StatusText;
    RAISE EXCEPTION
        USING DETAIL = 'ERROR:Change Failed [' || SQLSTATE|| ']' || SQLERRM;
        --transaction is rolled back;
END
$CR$;

/*
Output will be:
NOTICE:  CORRECT Server and DB : Server [flexible-pip-data-management-prod], Database [datamanagement]

NOTICE:  DTSRD-xxxxx Step 1 : Rows INSERT by query=25
NOTICE:  DTSRD-xxxxx SUCCESSFUL COMPLETION, all changes COMMITTED.
*/
