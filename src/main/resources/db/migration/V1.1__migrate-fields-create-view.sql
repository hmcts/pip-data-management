--
-- Create the table if doesn't exist. Only used in test DBs
--
CREATE TABLE IF NOT EXISTS artefact (
     artefact_id uuid NOT NULL PRIMARY KEY,
     content_date timestamp,
     display_from timestamp,
     display_to timestamp,
     is_flat_file boolean,
     language varchar(255),
     list_type varchar(255),
     location_id varchar(255),
     payload varchar(255),
     provenance varchar(255),
     search json,
     sensitivity varchar(255),
     source_artefact_id varchar(255),
     type varchar(255)
);

--
-- If the table already exists without the new columns, add them in
--
ALTER TABLE artefact
  ADD COLUMN IF NOT EXISTS isArchived boolean,
  ADD COLUMN IF NOT EXISTS lastReceivedDate timestamp,
  ADD COLUMN IF NOT EXISTS supersededCount integer;

--
-- Set isArchived to false if it's not already been set
--
UPDATE artefact
SET isArchived = false
WHERE artefact.isArchived IS NULL;

--
-- Set lastReceivedDate to Display From date if it's not already been set
--
UPDATE artefact
SET lastReceivedDate = display_from
WHERE artefact.lastReceivedDate IS NULL;

--
-- Set supersededCount to 0 if it's not already been set
--
UPDATE artefact
SET supersededCount = 0
WHERE artefact.supersededCount IS NULL;

--
-- Create materialised view for SDP
--
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
       artefact.isArchived,
       artefact.lastReceivedDate,
       artefact.supersededCount
FROM artefact;
