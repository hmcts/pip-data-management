CREATE TABLE IF NOT EXISTS location_reference (
    location_reference_id uuid NOT NULL PRIMARY KEY,
    provenance varchar(255),
    provenance_location_id varchar(255),
    provenance_location_type varchar(255),
    location_id integer,
    CONSTRAINT fk_location_id
      FOREIGN KEY (location_id)
        REFERENCES location (location_id)
);
