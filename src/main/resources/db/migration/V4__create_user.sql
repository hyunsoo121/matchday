CREATE TYPE oauth_provider AS ENUM ('KAKAO', 'GOOGLE');
CREATE TYPE favorite_target_type AS ENUM ('TEAM', 'SPORT', 'LEAGUE');

CREATE TABLE "user" (
  id              BIGSERIAL       PRIMARY KEY,
  oauth_provider  oauth_provider  NOT NULL,
  oauth_id        VARCHAR(100)    NOT NULL,
  nickname        VARCHAR(50)     NOT NULL,
  email           VARCHAR(100),
  created_at      TIMESTAMP       NOT NULL DEFAULT now(),
  UNIQUE (oauth_provider, oauth_id)
);

CREATE TABLE user_favorite (
  id          BIGSERIAL             PRIMARY KEY,
  user_id     BIGINT                NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  target_type favorite_target_type  NOT NULL,
  target_id   BIGINT                NOT NULL,
  created_at  TIMESTAMP             NOT NULL DEFAULT now(),
  UNIQUE (user_id, target_type, target_id)
);

CREATE INDEX idx_user_favorite_user_id ON user_favorite(user_id);
