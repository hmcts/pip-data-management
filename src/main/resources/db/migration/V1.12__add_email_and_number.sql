--
-- If the table already exists without the new columns, add them in
--
ALTER TABLE location
  ADD COLUMN IF NOT EXISTS contact_no varchar(255),
  ADD COLUMN IF NOT EXISTS email varchar(255)
