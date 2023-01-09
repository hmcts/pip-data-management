--
-- Add is_flat_file to the Artefact Materialised View
--
DROP MATERIALIZED VIEW IF EXISTS sdp_mat_view_artefact;

CREATE MATERIALIZED VIEW IF NOT EXISTS sdp_mat_view_artefact AS
SELECT artefact.artefact_id,
       artefact.display_from,
       artefact.display_to,
       artefact.language,
       artefact.provenance,
       artefact.sensitivity,
       artefact.source_artefact_id,
       artefact.type,
       artefact.content_date,
       artefact.location_id,
       artefact.list_type,
       artefact.superseded_count,
       artefact.last_received_date,
       artefact.is_archived,
       artefact.is_flat_file
FROM artefact;
