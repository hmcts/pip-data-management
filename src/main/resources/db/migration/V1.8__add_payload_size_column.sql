ALTER TABLE artefact
  ADD COLUMN IF NOT EXISTS payload_size numeric(10,2);
