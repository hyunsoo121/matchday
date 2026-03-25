-- match.status: match_status enum → varchar + check constraint
ALTER TABLE match ALTER COLUMN status TYPE varchar(20) USING status::varchar;
ALTER TABLE match ALTER COLUMN status SET DEFAULT 'SCHEDULED';
ALTER TABLE match ADD CONSTRAINT match_status_check
  CHECK (status IN ('SCHEDULED', 'LIVE', 'FINISHED', 'POSTPONED'));

-- match_team.role: match_team_role enum → varchar + check constraint
ALTER TABLE match_team ALTER COLUMN role TYPE varchar(20) USING role::varchar;
ALTER TABLE match_team ADD CONSTRAINT match_team_role_check
  CHECK (role IN ('HOME', 'AWAY', 'PARTICIPANT'));

-- sport.category: sport_category enum → varchar + check constraint
ALTER TABLE sport ALTER COLUMN category TYPE varchar(20) USING category::varchar;
ALTER TABLE sport ADD CONSTRAINT sport_category_check
  CHECK (category IN ('sports', 'esports', 'motorsports'));

-- user.oauth_provider: oauth_provider enum → varchar + check constraint
ALTER TABLE "user" ALTER COLUMN oauth_provider TYPE varchar(20) USING oauth_provider::varchar;
ALTER TABLE "user" ADD CONSTRAINT oauth_provider_check
  CHECK (oauth_provider IN ('KAKAO', 'GOOGLE'));

-- user_favorite.target_type: favorite_target_type enum → varchar + check constraint
ALTER TABLE user_favorite ALTER COLUMN target_type TYPE varchar(20) USING target_type::varchar;
ALTER TABLE user_favorite ADD CONSTRAINT favorite_target_type_check
  CHECK (target_type IN ('TEAM', 'SPORT', 'LEAGUE'));
