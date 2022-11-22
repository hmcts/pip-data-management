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
  ADD COLUMN IF NOT EXISTS is_archived boolean,
  ADD COLUMN IF NOT EXISTS last_received_date timestamp,
  ADD COLUMN IF NOT EXISTS superseded_count integer;

--
-- Set isArchived to false if it's not already been set
--
UPDATE artefact
SET is_archived = false
WHERE artefact.is_archived IS NULL;

--
-- Set lastReceivedDate to Display From date if it's not already been set
--
UPDATE artefact
SET last_received_date = display_from
WHERE artefact.last_received_date IS NULL;

--
-- Set supersededCount to 0 if it's not already been set
--
UPDATE artefact
SET superseded_count = 0
WHERE artefact.superseded_count IS NULL;
