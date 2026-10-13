--
-- Remove old CROWN_WARNED_PDDA_LIST entry from the artefact table
--
DELETE From artefact
WHERE list_type = 'CROWN_WARNED_PDDA_LIST';
