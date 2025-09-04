--
-- Add artefact_archived to the Artefact Materialised View so it can return all the artefacts
--
DROP MATERIALIZED VIEW IF EXISTS sdp_mat_view_artefact;

CREATE MATERIALIZED VIEW sdp_mat_view_artefact AS
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
       false AS is_archived
FROM artefact
UNION ALL
SELECT artefact_archived.artefact_id,
       artefact_archived.display_from,
       artefact_archived.display_to,
       artefact_archived.language,
       artefact_archived.provenance,
       artefact_archived.sensitivity,
       '' AS source_artefact_id,
       artefact_archived.type,
       artefact_archived.content_date,
       artefact_archived.location_id,
       artefact_archived.list_type,
       artefact_archived.superseded_count,
       artefact_archived.last_received_date,
       true AS is_archived
FROM artefact_archived;
