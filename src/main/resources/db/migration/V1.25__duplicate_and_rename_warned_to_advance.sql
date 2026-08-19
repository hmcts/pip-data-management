INSERT INTO subscription (id, user_id, search_type, search_value, channel, created_date, case_number, case_name,
                          last_updated_date)
SELECT md5(random()::text || clock_timestamp()::text)::uuid as id,
  user_id,
       'CASE_NUMBER',
       search_value,
       channel,
       created_date,
       case_number,
       case_name,
       last_updated_date
FROM subscription
WHERE search_type = 'CASE_ID';
