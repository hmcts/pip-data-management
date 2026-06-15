CREATE TABLE IF NOT EXISTS artefact_search (
 id uuid NOT NULL PRIMARY KEY,
 artefact_id uuid NOT NULL,
 case_number varchar(255),
  case_name varchar(255),

  CONSTRAINT fk_artefact_search_artefact_id
  FOREIGN KEY (artefact_id)
  REFERENCES artefact (artefact_id)
  ON DELETE CASCADE
  );

CREATE INDEX IF NOT EXISTS artefact_search_artefact_id_idx
  ON artefact_search (artefact_id);
