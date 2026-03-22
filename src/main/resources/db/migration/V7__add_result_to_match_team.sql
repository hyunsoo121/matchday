ALTER TABLE match_team ADD COLUMN IF NOT EXISTS result VARCHAR(10);
ALTER TABLE match_team DROP CONSTRAINT IF EXISTS match_team_result_check;
ALTER TABLE match_team ADD CONSTRAINT match_team_result_check CHECK (result IN ('WIN', 'LOSS', 'DRAW'));
