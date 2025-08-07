--
-- Migration script to archive artefact data
--
INSERT INTO artefact_archived (artefact_id, content_date, display_from, display_to, is_flat_file,
                               "language", list_type,location_id,  provenance, sensitivity, type,
                               last_received_date, superseded_count, archived_date, is_manually_deleted)
SELECT artefact_id, content_date, display_from, display_to, is_flat_file,
       "language", list_type,location_id, provenance, sensitivity, type,
       last_received_date, superseded_count, LOCALTIMESTAMP, TRUE
FROM artefact
WHERE is_archived = TRUE;
