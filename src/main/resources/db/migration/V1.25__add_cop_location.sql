INSERT INTO location (location_id, name, region, location_type, jurisdiction, jurisdiction_type, welsh_name, welsh_region, welsh_jurisdiction, welsh_jurisdiction_type)
VALUES (390, 'Court of Protection', '{"London", "South East", "South West", "North East", "North West", "Midlands", "East Midlands", "West Midlands", "Yorkshire and Humber", "Wales", "Scotland", "National"}', 'VENUE', '{"Court of Protection"}', '{"Court of Protection"}', 'Llys Gwarchod', '{"Llundain", "De Ddwyrain", "De Orllewin", "Gogledd Ddwyrain", "Gogledd Orllewin", "Canolbarth Lloegr", "Dwyrain Canolbarth Lloegr", "Gorllewin Canolbarth Lloegr", "Swydd Efrog a''r Hwmbr", "Cymru", "Yr Alban", "Cenedlaethol"}', '{"Llys Gwarchod"}', '{"Llys Gwarchod"}')
ON CONFLICT (location_id) DO NOTHING;

-- Recreate the materialized view to include all current columns
DROP MATERIALIZED VIEW IF EXISTS sdp_mat_view_location;
CREATE MATERIALIZED VIEW sdp_mat_view_location AS
SELECT location.location_id,
       location.name,
       location.region,
       location.location_type,
       location.jurisdiction,
       location.jurisdiction_type,
       location.welsh_name,
       location.welsh_jurisdiction,
       location.welsh_jurisdiction_type,
       location.welsh_region,
       location.email,
       location.contact_no
FROM location;
