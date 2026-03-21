CREATE TYPE match_status AS ENUM ('SCHEDULED', 'LIVE', 'FINISHED', 'POSTPONED');
CREATE TYPE match_team_role AS ENUM ('HOME', 'AWAY', 'PARTICIPANT');

CREATE TABLE match (
  id            BIGSERIAL     PRIMARY KEY,
  sport_id      BIGINT        NOT NULL REFERENCES sport(id),
  league_id     BIGINT        REFERENCES league(id),
  match_time    TIMESTAMP     NOT NULL,
  status        match_status  NOT NULL DEFAULT 'SCHEDULED',
  score_detail  JSONB,
  external_id   VARCHAR(50),
  external_url  VARCHAR(255),
  created_at    TIMESTAMP     NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE match_team (
  id            BIGSERIAL       PRIMARY KEY,
  match_id      BIGINT          NOT NULL REFERENCES match(id),
  team_id       BIGINT          NOT NULL REFERENCES team(id),
  role          match_team_role NOT NULL,
  total_score   INT
);

CREATE INDEX idx_match_match_time  ON match(match_time);
CREATE INDEX idx_match_status      ON match(status);
CREATE INDEX idx_match_sport_id    ON match(sport_id);
CREATE INDEX idx_match_league_id   ON match(league_id);
CREATE INDEX idx_match_team_match  ON match_team(match_id);
