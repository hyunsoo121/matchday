CREATE TABLE team (
  id            BIGSERIAL     PRIMARY KEY,
  sport_id      BIGINT        NOT NULL REFERENCES sport(id),
  league_id     BIGINT        REFERENCES league(id),
  name_ko       VARCHAR(100)  NOT NULL,
  name_en       VARCHAR(100)  NOT NULL,
  short_name    VARCHAR(20),
  logo_url      VARCHAR(255),
  external_id   VARCHAR(50),
  created_at    TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_team_sport_id  ON team(sport_id);
CREATE INDEX idx_team_league_id ON team(league_id);
