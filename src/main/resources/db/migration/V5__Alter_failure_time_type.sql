ALTER TABLE launch_failures
ALTER COLUMN time TYPE INTEGER USING (time::integer);