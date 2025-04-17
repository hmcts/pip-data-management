CREATE TABLE IF NOT EXISTS location_metadata (
  location_metadata_id uuid NOT NULL PRIMARY KEY,
  location_id integer NOT NULL,
  caution_message varchar(511),
  welsh_caution_message varchar(511),
  no_list_message varchar(511),
  welsh_no_list_message varchar(511),
  CONSTRAINT fk_location_id
  FOREIGN KEY (location_id)
  REFERENCES location (location_id)
);
