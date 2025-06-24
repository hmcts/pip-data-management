--
-- Create the table if doesn't exist.
--
CREATE TABLE IF NOT EXISTS location_metadata (
    location_metadata_id  uuid NOT NULL PRIMARY KEY,
    location_id integer,
    CONSTRAINT fk_location_id
      FOREIGN KEY (location_id)
        REFERENCES location (location_id),
    CONSTRAINT unique_location_id_constraint
      UNIQUE (location_id),
    caution_message       text,
    welsh_caution_message text,
    no_list_message       text,
    welsh_no_list_message text
);
