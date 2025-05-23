ALTER TABLE location
  ADD COLUMN IF NOT EXISTS jurisdiction_type text[],
  ADD COLUMN IF NOT EXISTS welsh_jurisdiction_type text[]
