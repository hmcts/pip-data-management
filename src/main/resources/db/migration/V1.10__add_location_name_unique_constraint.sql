--
-- Add unique constraints for location name and Welsh location name so only one can exists when we upload
-- reference data.
--
ALTER TABLE location
  DROP CONSTRAINT IF EXISTS unique_location_name_constraint,
  ADD CONSTRAINT unique_location_name_constraint UNIQUE (name),
  DROP CONSTRAINT IF EXISTS unique_welsh_location_name_constraint,
  ADD CONSTRAINT unique_welsh_location_name_constraint UNIQUE (welsh_name);
