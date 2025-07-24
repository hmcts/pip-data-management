ALTER TABLE location
  DROP CONSTRAINT IF EXISTS unique_location_name_constraint,
  DROP CONSTRAINT IF EXISTS unique_welsh_location_name_constraint;
