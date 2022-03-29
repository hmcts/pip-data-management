CREATE TABLE IF NOT EXISTS court (
  court_id integer NOT NULL PRIMARY KEY,
  jurisdiction text[],
  name character varying(255),
  region character varying(255)
);

INSERT INTO court(court_id, jurisdiction, name)
VALUES(0, '{}', 'Single Justice Procedure (SJP)')
ON CONFLICT (court_id) DO NOTHING;
