--
-- Creates the materialised view for location if it doesn't exist.
--
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
