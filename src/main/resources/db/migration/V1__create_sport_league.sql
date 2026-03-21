CREATE TYPE sport_category AS ENUM ('sports', 'esports', 'motorsports');

CREATE TABLE sport (
  id            BIGSERIAL     PRIMARY KEY,
  code          VARCHAR(20)   NOT NULL UNIQUE,
  name_ko       VARCHAR(50)   NOT NULL,
  name_en       VARCHAR(50)   NOT NULL,
  icon_url      VARCHAR(255),
  category      sport_category NOT NULL,
  is_active     BOOLEAN       NOT NULL DEFAULT true,
  display_order INT           NOT NULL DEFAULT 0
);

CREATE TABLE league (
  id            BIGSERIAL     PRIMARY KEY,
  sport_id      BIGINT        NOT NULL REFERENCES sport(id),
  code          VARCHAR(30)   NOT NULL UNIQUE,
  name_ko       VARCHAR(100)  NOT NULL,
  name_en       VARCHAR(100)  NOT NULL,
  country       VARCHAR(50),
  logo_url      VARCHAR(255),
  is_active     BOOLEAN       NOT NULL DEFAULT true,
  display_order INT           NOT NULL DEFAULT 0
);

CREATE INDEX idx_league_sport_id ON league(sport_id);
