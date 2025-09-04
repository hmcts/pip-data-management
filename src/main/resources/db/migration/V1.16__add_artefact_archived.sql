--
-- Create artefact_archived table if it doesn't exist.
--
CREATE TABLE IF NOT EXISTS artefact_archived (
      artefact_id UUID PRIMARY KEY,
      content_date TIMESTAMP,
      display_from TIMESTAMP,
      display_to TIMESTAMP,
      is_flat_file BOOLEAN,
      language VARCHAR(255),
      list_type VARCHAR(255),
      location_id VARCHAR(255),
      provenance VARCHAR(255),
      sensitivity VARCHAR(255),
      type VARCHAR(255),
      last_received_date TIMESTAMP,
      superseded_count INTEGER,
      archived_date TIMESTAMP,
      is_manually_deleted BOOLEAN DEFAULT FALSE
);
