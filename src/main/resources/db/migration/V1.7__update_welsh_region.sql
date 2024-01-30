--
-- An issue with the welsh region data type was found in script 1.5. This is now incompatible with the latest hibernate
-- version. This script fixes the issue by changing the data type to text[] and re-creating the materialized view.
--
DROP MATERIALIZED VIEW IF EXISTS sdp_mat_view_location;

ALTER TABLE location
ALTER COLUMN welsh_region TYPE text[] USING welsh_region::text[];

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

