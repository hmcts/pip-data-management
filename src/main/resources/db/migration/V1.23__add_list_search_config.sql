--
-- Create the table if doesn't exist.
--
CREATE TABLE IF NOT EXISTS list_search_config (
  id uuid NOT NULL PRIMARY KEY,
  list_type varchar(255),
  CONSTRAINT unique_list_type_constraint
    UNIQUE (list_type),
  case_number_field_name varchar(255),
  case_name_field_name varchar(255)
);
