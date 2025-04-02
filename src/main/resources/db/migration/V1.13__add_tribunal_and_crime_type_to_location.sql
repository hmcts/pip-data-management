ALTER TABLE location
  ADD COLUMN IF NOT EXISTS tribunal_type text[],
  ADD COLUMN IF NOT EXISTS crime_type text[],
  ADD COLUMN IF NOT EXISTS welsh_tribunal_type text[],
  ADD COLUMN IF NOT EXISTS welsh_crime_type text[]
