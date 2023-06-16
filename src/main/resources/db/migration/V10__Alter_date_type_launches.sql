ALTER TABLE launches ALTER COLUMN updated_at TYPE DATE USING (updated_at::DATE);
