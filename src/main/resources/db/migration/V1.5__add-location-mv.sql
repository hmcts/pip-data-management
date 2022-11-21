--
-- Creates the materialised view for location if it doesn't exist.
--
CREATE TABLE IF NOT EXISTS location (
    location_id integer NOT NULL PRIMARY KEY,
    name varchar(255),
    region text[],
    location_type varchar(255),
    jurisdiction text[],
    welsh_name varchar(255),
    welsh_jurisdiction text[],
    welsh_region varchar(255)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS sdp_mat_view_location AS
SELECT location.location_id,
       location.name,
       location.region,
       location.location_type,
       location.jurisdiction,
       location.welsh_name,
       location.welsh_jurisdiction,
       location.welsh_region
FROM location;
